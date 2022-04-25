package com.alibaba.tdata.aisp.server.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tdata.aisp.server.common.condition.TaskQueryCondition;
import com.alibaba.tdata.aisp.server.common.constant.AnalyseTaskStatusEnum;
import com.alibaba.tdata.aisp.server.common.constant.AnalyseTaskTypeEnum;
import com.alibaba.tdata.aisp.server.common.constant.CodeTypeEnum;
import com.alibaba.tdata.aisp.server.common.exception.DetectorException;
import com.alibaba.tdata.aisp.server.common.exception.PlatformInternalException;
import com.alibaba.tdata.aisp.server.common.properties.AuthProperties;
import com.alibaba.tdata.aisp.server.common.properties.EnvProperties;
import com.alibaba.tdata.aisp.server.common.properties.TaskRemainProperties;
import com.alibaba.tdata.aisp.server.common.utils.DetectorUtil;
import com.alibaba.tdata.aisp.server.common.utils.MessageDigestUtil;
import com.alibaba.tdata.aisp.server.common.utils.TaskCacheUtil;
import com.alibaba.tdata.aisp.server.common.utils.TeslaAuthUtil;
import com.alibaba.tdata.aisp.server.controller.param.AnalyseInstanceCreateParam;
import com.alibaba.tdata.aisp.server.controller.param.AnalyseTaskCreateParam;
import com.alibaba.tdata.aisp.server.controller.param.AnalyseTaskCreateParam.AnalyseTaskCreateParamBuilder;
import com.alibaba.tdata.aisp.server.controller.param.AnalyzeTaskUpdateParam;
import com.alibaba.tdata.aisp.server.controller.param.CodeParam;
import com.alibaba.tdata.aisp.server.controller.param.TaskQueryParam;
import com.alibaba.tdata.aisp.server.controller.param.TaskTrendQueryParam;
import com.alibaba.tdata.aisp.server.controller.result.TaskQueryResult;
import com.alibaba.tdata.aisp.server.controller.result.TaskReportResult;
import com.alibaba.tdata.aisp.server.repository.AnalyseInstanceRepository;
import com.alibaba.tdata.aisp.server.repository.AnalyseTaskRepository;
import com.alibaba.tdata.aisp.server.repository.SceneRepository;
import com.alibaba.tdata.aisp.server.repository.domain.DetectorConfigDO;
import com.alibaba.tdata.aisp.server.repository.domain.InstanceDO;
import com.alibaba.tdata.aisp.server.repository.domain.SceneConfigDO;
import com.alibaba.tdata.aisp.server.repository.domain.TaskDO;
import com.alibaba.tdata.aisp.server.repository.domain.TaskTrendDO;
import com.alibaba.tdata.aisp.server.service.AnalyseExecuteService;
import com.alibaba.tdata.aisp.server.service.AnalyseInstanceService;
import com.alibaba.tdata.aisp.server.service.AnalyseTaskService;
import com.alibaba.tdata.aisp.server.service.DetectorService;

import com.alicp.jetcache.Cache;
import com.hubspot.jinjava.Jinjava;
import io.micrometer.core.instrument.util.IOUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @ClassName: AnalyseExecuteServiceImpl
 * @Author: dyj
 * @DATE: 2021-11-15
 * @Description:
 **/
@Slf4j
@Service
public class AnalyseExecuteServiceImpl implements AnalyseExecuteService {
    private static final String CODE_TEMPLATE_FILE_PATH = "code/%s.tpl";
    private static OkHttpClient syncHttpClient;
    private static OkHttpClient asyncHttpClient;

    @Autowired
    private AnalyseInstanceService instanceService;
    @Autowired
    private AnalyseInstanceRepository instanceRepository;
    @Autowired
    private SceneRepository sceneRepository;
    @Autowired
    private DetectorService detectorService;
    @Autowired
    private AnalyseTaskService taskService;
    @Autowired
    private AnalyseTaskRepository taskRepository;
    @Autowired
    private AuthProperties authProperties;
    @Autowired
    private EnvProperties envProperties;
    @Autowired
    @Qualifier("taskResultCache")
    private Cache<String, JSONObject> taskResultCache;

    /**
     * @param param
     * @param sceneCode
     * @param detectorCode
     * @return
     */
    @Override
    public JSONObject exec(String taskUUID, JSONObject param, String sceneCode, String detectorCode) {
        SceneConfigDO sceneConfigDO = sceneRepository.queryById(sceneCode);
        if (sceneConfigDO==null) {
            throw new IllegalArgumentException("action=analyze || Path variable：sceneCode: "+sceneCode+" is not exists!");
        }
        checkExecParam(param);
        AnalyseTaskCreateParamBuilder taskBuilder = AnalyseTaskCreateParam.builder().reqParam(param);
        JSONObject modelParam = param.containsKey("modelParam")?param.getJSONObject("modelParam"):new JSONObject();
        // 加入场景级别参数
        if (!StringUtils.isEmpty(sceneConfigDO.getSceneModelParam())) {
            modelParam.putAll(JSONObject.parseObject(sceneConfigDO.getSceneModelParam()));
        }
        // 加入实例级别参数
        if (!StringUtils.isEmpty(param.getString("entityId"))) {
            InstanceDO instanceDO = instanceService.queryById(MessageDigestUtil.genSHA256(sceneCode
                    .concat(detectorCode)
                    .concat(param.getString("entityId"))));
            if (null == instanceDO) {
                String instanceCode = instanceService.create(AnalyseInstanceCreateParam.builder()
                    .sceneCode(sceneCode)
                    .detectorCode(detectorCode)
                    .entityId(param.getString("entityId"))
                    .build());
                taskBuilder.instanceCode(instanceCode);
            } else {
                if (!StringUtils.isEmpty(instanceDO.getModelParam())) {
                    modelParam.putAll(JSONObject.parseObject(instanceDO.getModelParam()));
                }
                // 加入用户最近反馈信息
                if (!StringUtils.isEmpty(instanceDO.getRecentFeedback())) {
                    JSONArray recentFeedback = JSONArray.parseArray(instanceDO.getRecentFeedback());
                    if (modelParam.containsKey("feedback")) {
                        JSONArray feedback = modelParam.getJSONArray("feedback");
                        feedback.addAll(recentFeedback);
                        modelParam.put("feedback", feedback);
                    } else {
                        modelParam.put("feedback", recentFeedback);
                    }
                }
                taskBuilder.instanceCode(instanceDO.getInstanceCode());
            }
        }
        param.put("modelParam", modelParam);
        assert StringUtils.isEmpty(param.getString("taskType"));
        AnalyseTaskTypeEnum taskTypeEnum = AnalyseTaskTypeEnum.fromValue(param.getString("taskType"));
        taskService.create(taskBuilder.reqParam(param)
            .taskUUID(taskUUID)
            .sceneCode(sceneCode)
            .detectorCode(detectorCode)
            .taskTypeEnum(taskTypeEnum)
            .taskStatusEnum(AnalyseTaskStatusEnum.RUNNING)
            .build());
        switch (taskTypeEnum) {
            case SYNC: {
                return syncExec(taskUUID, detectorCode, param);
            }
            case ASYNC: {
                return asyncExec(taskUUID, detectorCode, param);
            }
            default:
                throw new IllegalArgumentException("action=exec|| Can not find task type:" + taskTypeEnum);
        }
    }

    private void checkExecParam(JSONObject param) {
        if (StringUtils.isEmpty(param.getString("taskType"))) {
            param.put("taskType", "sync");
        }
    }

    @Override
    public String code(CodeParam param)
        throws IOException, NoSuchAlgorithmException {
        String noticeTpl = loadClassPathFile(String.format(CODE_TEMPLATE_FILE_PATH, param.getCodeType()));
        JSONObject parameters = new JSONObject();
        if (CodeTypeEnum.CURL.getValue().equalsIgnoreCase(param.getCodeType())){
            parameters.put("input", param.getInput().toString());
        } else {
            parameters.put("input", JSONObject.toJSONString(param.getInput().toJSONString()));
        }
        parameters.put("url", param.getUrl());
        parameters.put("app", authProperties.getClientId());
        parameters.put("key", authProperties.getClientSecret());
        parameters.put("user", authProperties.getUser());
        parameters.put("passwd", TeslaAuthUtil.getPasswdHash(authProperties.getUser(), authProperties.getPassword()));
        return jinjaRender(noticeTpl, parameters);
    }

    @Override
    public JSONObject getInput(String detectorCode) {
        return detectorService.getInput(detectorCode);
    }

    @Override
    public String getDoc(String detectorCode) {
        return detectorService.getDoc(detectorCode);
    }

    private JSONObject syncExec(String taskUUID, String detectorCode, JSONObject param) {
        String detectorUrl = System.getenv(detectorCode);
        if (StringUtils.isEmpty(detectorUrl)){
            DetectorConfigDO detectorConfigDO = detectorService.queryById(detectorCode);
            if (detectorConfigDO==null){
                throw new IllegalArgumentException(
                    "action=syncExec || Can not find detector record from detectorCode:" + detectorCode);
            }
            detectorUrl = detectorConfigDO.getDetectorUrl();
        }
        JSONObject postJson = JSONObject.parseObject(JSONObject.toJSONString(param));
        postJson.put("taskUUID", taskUUID);
        taskResultCache.put(TaskCacheUtil.genReqKey(taskUUID), postJson);
        OkHttpClient syncHttpClient = getSyncHttpClient();
        JSONObject headers = new JSONObject();
        Request.Builder requestBuilder = createRequestBuilder(
            "http://".concat(detectorUrl).concat("/analyze"), null, headers);
        Request request = requestBuilder.post(RequestBody.create(postJson.toJSONString(),
            MediaType.parse("application/json"))).build();
        Response response;
        try {
            response = syncHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                assert response.body() != null;
                String bodyStr = response.body().string();
                JSONObject result = JSONObject.parseObject(bodyStr);
                taskResultCache.put(TaskCacheUtil.genResultKey(taskUUID), result);
                log.info("action=syncExec || detector http code:{} !", response.code());
                if (response.code()==200){
                    completeTask(taskUUID, AnalyseTaskStatusEnum.SUCCESS, result);
                }
                result.put("taskUUID", taskUUID);
                return result;
            } else {
                assert response.body() != null;
                String bodyStr = response.body().string();
                JSONObject result = new JSONObject();
                if (checkBody(bodyStr)) {
                    result = JSONObject.parseObject(bodyStr);
                } else {
                    result.put("code", response.code());
                    result.put("message", response.message());
                }
                result.put("code", "Detector_".concat(result.getString("code")));
                taskResultCache.put(TaskCacheUtil.genResultKey(taskUUID), result);
                updateTaskStatus(taskUUID, AnalyseTaskStatusEnum.FAILED, result);
                result.put("taskUUID", taskUUID);
                log.info("action=syncExec || result: {}", result);
                throw new DetectorException(response.code(), result);
            }

        } catch (IOException e) {
            JSONObject result = new JSONObject();
            result.put("message", e.getMessage());
            updateTaskStatus(taskUUID, AnalyseTaskStatusEnum.FAILED, result);
            log.error("action=syncExec|| Send request have error!", e);
            throw new PlatformInternalException(taskUUID,
                "action=syncExec|| Send request have error! " + e.getMessage(), e);
        }
    }

    private void completeTask(String taskUUID, AnalyseTaskStatusEnum taskStatusEnum, JSONObject result) {
        log.info("action=completeTask|| taskUuid: {}, status:{} !", taskUUID, taskStatusEnum.getValue());
        TaskDO taskDO = taskRepository.queryById(taskUUID);
        assert taskDO != null;
        Date now = new Date();
        long cost = now.getTime() - taskDO.getGmtCreate().getTime();
        taskDO.setTaskUuid(taskUUID);
        taskDO.setTaskStatus(taskStatusEnum.getValue());
        taskDO.setCostTime(cost);
        taskDO.setTaskResult(result.toJSONString());
        taskRepository.updateSelectiveById(taskDO);
        JSONObject newModelParam = result.getJSONObject("data").getJSONObject("modelParam");
        result.getJSONObject("data").remove("modelParam");
        String instanceCode = taskDO.getInstanceCode();
        if (newModelParam!=null && !StringUtils.isEmpty(instanceCode)){
            InstanceDO instanceDO = instanceService.queryById(instanceCode);
            instanceDO.setModelParam(JSONObject.toJSONString(newModelParam));
            instanceRepository.updateById(instanceDO);
        }
    }

    private JSONObject asyncExec(String taskUUID, String detectorCode, JSONObject param) {
        String detectorUrl = System.getenv(detectorCode);
        if (StringUtils.isEmpty(detectorUrl)){
            DetectorConfigDO detectorConfigDO = detectorService.queryById(detectorCode);
            if (detectorConfigDO==null){
                throw new IllegalArgumentException(
                    "action=syncExec || Can not find detector record from detectorCode:" + detectorCode);
            }
            detectorUrl = detectorConfigDO.getDetectorUrl();
        }
        JSONObject postJson = JSONObject.parseObject(JSONObject.toJSONString(param));
        postJson.put("taskUUID", taskUUID);
        taskResultCache.put(TaskCacheUtil.genReqKey(taskUUID), postJson);
        OkHttpClient asyncHttpClient = getAsyncHttpClient();
        JSONObject headers = new JSONObject();
        String url = DetectorUtil.buildUrl(envProperties.getStageId(), detectorUrl);
        Request.Builder requestBuilder = createRequestBuilder(
            "http://".concat(url).concat("/analyze"), null, headers);
        Request request = requestBuilder.post(RequestBody.create(postJson.toJSONString(),
            MediaType.parse("application/json"))).build();
        JSONObject asyncRes = new JSONObject();
        asyncHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                log.error("action=asyncExec || ", e);
                JSONObject result = new JSONObject();
                result.put("message", e.getMessage());
                updateTaskStatus(taskUUID, AnalyseTaskStatusEnum.FAILED, result);
            }

            @SneakyThrows
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String bodyStr = response.body().string();
                    JSONObject result = JSONObject.parseObject(bodyStr);
                    taskResultCache.put(TaskCacheUtil.genResultKey(taskUUID), result);
                    if (response.code()==200){
                        completeTask(taskUUID, AnalyseTaskStatusEnum.SUCCESS, result);
                    }
                } else {
                    assert response.body() != null;
                    String bodyStr = response.body().string();
                    JSONObject result = new JSONObject();
                    if (checkBody(bodyStr)) {
                        result = JSONObject.parseObject(bodyStr);
                    } else {
                        result.put("code", response.code());
                        result.put("message", response.message());
                    }
                    result.put("code", "Detector_".concat(result.getString("code")));
                    log.info("action=asyncExec || result: {}", result);
                    taskResultCache.put(TaskCacheUtil.genResultKey(taskUUID), result);
                    updateTaskStatus(taskUUID, AnalyseTaskStatusEnum.FAILED, result);
                }
            }
        });
        asyncRes.put("taskUUID", taskUUID);
        return asyncRes;
    }

    private boolean checkBody(String body) {
        if (StringUtils.isEmpty(body)) {
            return false;
        }
        if (body.startsWith("{") && body.endsWith("}")) {
            return true;
        }
        return false;
    }

    private void updateTaskStatus(String taskUUID, AnalyseTaskStatusEnum taskStatusEnum, JSONObject result) {
        TaskDO taskDO = taskRepository.queryById(taskUUID);
        assert taskDO != null;
        Date now = new Date();
        long cost = now.getTime() - taskDO.getGmtCreate().getTime();
        taskDO.setTaskUuid(taskUUID);
        taskDO.setTaskStatus(taskStatusEnum.getValue());
        taskDO.setCostTime(cost);
        taskDO.setTaskResult(result.toJSONString());
        taskRepository.updateSelectiveById(taskDO);
    }

    private String loadClassPathFile(String filePath) throws IOException {
        Resource config = new ClassPathResource(filePath);
        InputStream inputStream = config.getInputStream();
        return IOUtils.toString(inputStream, StandardCharsets.UTF_8);
    }

    private String jinjaRender(String raw, JSONObject parameters) {
        Jinjava jinjava = new Jinjava();
        return jinjava.render(raw, parameters);
    }

    private static OkHttpClient getSyncHttpClient() {
        if (syncHttpClient == null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1000);
            dispatcher.setMaxRequestsPerHost(1000);
            syncHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                .dispatcher(dispatcher)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .callTimeout(2, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
        }
        return syncHttpClient;
    }

    private static OkHttpClient getAsyncHttpClient() {
        if (asyncHttpClient == null) {
            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequests(1000);
            dispatcher.setMaxRequestsPerHost(1000);
            asyncHttpClient = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(32, 5, TimeUnit.MINUTES))
                .dispatcher(dispatcher)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.MINUTES)
                .writeTimeout(15, TimeUnit.MINUTES)
                .callTimeout(15, TimeUnit.MINUTES)
                .retryOnConnectionFailure(true)
                .build();
        }
        return asyncHttpClient;
    }

    private static Request.Builder createRequestBuilder(String url, JSONObject params, JSONObject headers) {
        if (params == null) {
            params = new JSONObject();
        }
        if (headers == null) {
            headers = new JSONObject();
        }
        HttpUrl.Builder queryUrl = Objects.requireNonNull(HttpUrl.parse(url)).newBuilder();
        for (String key : params.keySet()) {
            queryUrl.addQueryParameter(key, params.getString(key));
        }
        Request.Builder requestBuilder = new Request.Builder().url(queryUrl.build());
        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.getString(key));
        }
        return requestBuilder;
    }
}
