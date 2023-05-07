package com.alibaba.sreworks.job.master.controllers;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DTO.*;
import com.alibaba.sreworks.job.master.params.*;
import com.alibaba.sreworks.job.master.services.StreamJobService;
import com.alibaba.sreworks.job.master.services.VvpService;
import com.alibaba.sreworks.job.utils.JsonUtil;
import com.alibaba.sreworks.job.utils.YamlUtil;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.alibaba.sreworks.job.utils.PageUtil.pageable;

/**
 * @author jinghua.yjh
 */
@Slf4j
@RestController
@RequestMapping("/stream-job")
public class StreamJobController extends BaseController {

    @Autowired
    StreamJobService streamJobService;

    @Autowired
    VvpService vvpService;

    @RequestMapping(value = "create", method = RequestMethod.POST)
    public TeslaBaseResult create(@RequestBody StreamJobCreateParam param) throws Exception {
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        if (param.getTags() == null) {
            param.setTags(new JSONArray());
        }
        if (param.getDescription() == null) {
            param.setDescription("");
        }
        if (param.getOptions() == null) {
            param.setOptions(new JSONObject());
        }
        return buildSucceedResult(streamJobService.create(param));
    }

    @RequestMapping(value = "runtimes", method = RequestMethod.GET)
    public TeslaBaseResult runtimes(Integer page, Integer pageSize) throws Exception {
        Page<SreworksStreamJobRuntimeDTO> runtimes = streamJobService.runtimes(pageable(page, pageSize));
        return buildSucceedResult(JsonUtil.map(
                "page", runtimes.getNumber() + 1,
                "pageSize", runtimes.getSize(),
                "total", runtimes.getTotalElements(),
                "items", runtimes.getContent()
        ));
    }

    @RequestMapping(value = "runtime/{id}", method = RequestMethod.GET)
    public TeslaBaseResult runtimeGet(@PathVariable("id") Long runtimeId) throws Exception {
        SreworksStreamJobRuntimeDTO runtime = streamJobService.runtimeGetById(runtimeId);
        return buildSucceedResult(runtime);
    }

    @RequestMapping(value = "runtime", method = RequestMethod.POST)
    public TeslaBaseResult addRuntime(@RequestBody StreamJobRuntimeCreateParam param) throws Exception {
        SreworksStreamJobRuntimeDTO runtime = streamJobService.addRuntime(param);
        return buildSucceedResult(runtime);
    }

    @RequestMapping(value = "runtime/preview", method = RequestMethod.POST)
    public TeslaBaseResult runtimePreview(@RequestBody StreamJobRuntimeCreateParam param) throws Exception {
        JSONObject deployment = streamJobService.generateDeployment(
            StreamJobDeploymentParam.builder()
            .entryClass(param.getSettings().getString("entryClass"))
            .flinkImage(param.getSettings().getString("flinkImage"))
            .jarUri(param.getSettings().getString("jarUri"))
            .name(param.getName())
            .build()
        );

        return buildSucceedResult(YamlUtil.jsonObjectToYaml(deployment));
    }
    @RequestMapping(value = "artifacts", method = RequestMethod.GET)
    public TeslaBaseResult artifacts(StreamJobArtifactsParam param) throws Exception {
        JSONObject artifacts = vvpService.listArtifacts();
        JSONArray artifactList = artifacts.getJSONArray("artifacts");

        artifactList.sort((o1, o2) -> {
            String name1 = ((JSONObject) o1).getString("createTime");
            String name2 = ((JSONObject) o2).getString("createTime");
            return name1.compareTo(name2);
        });

        Collections.reverse(artifactList);

        if(param.getIsRespOptions()){
            return buildSucceedResult(JsonUtil.map(
                "options", artifactList
            ));
        }else{
            return buildSucceedResult(artifactList);
        }
    }

    @RequestMapping(value = "{id}/settings", method = RequestMethod.PUT)
    public TeslaBaseResult updateSettings(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSourceUpdateSettingsParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.updateSettings(job, param.getSettings()));
    }

    @RequestMapping(value = "{id}/template", method = RequestMethod.PUT)
    public TeslaBaseResult updateTemplate(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSourceUpdateTemplateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.updateTemplate(job, param.getTemplateContent()));
    }

    @RequestMapping(value = "{id}/source", method = RequestMethod.POST)
    public TeslaBaseResult addSource(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSourceCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addSource(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/block/{blockId}", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteBlock(@PathVariable("id") Long streamJobId, @PathVariable("blockId") Long streamJobBlockId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        streamJobService.deleteBlock(streamJobBlockId);
        return buildSucceedResult(JsonUtil.map(
            "streamJobId", streamJobId,
                "blockId", streamJobBlockId
        ));
    }

    @RequestMapping(value = "{id}/sink", method = RequestMethod.POST)
    public TeslaBaseResult addSink(@PathVariable("id") Long streamJobId, @RequestBody StreamJobSinkCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addSink(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/python", method = RequestMethod.POST)
    public TeslaBaseResult addPython(@PathVariable("id") Long streamJobId, @RequestBody StreamJobPythonCreateParam param) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        param.setCreator(getUserEmployeeId());
        param.setOperator(getUserEmployeeId());
        return buildSucceedResult(streamJobService.addPython(streamJobId, job.getAppId(), param));
    }

    @RequestMapping(value = "{id}/python/template", method = RequestMethod.GET)
    public TeslaBaseResult pythonTemplate(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        return buildSucceedResult(streamJobService.pythonTemplate());
    }

    @RequestMapping(value = "{id}/blocks", method = RequestMethod.GET)
    public TeslaBaseResult getBlocks(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        return buildSucceedResult(streamJobService.listBlockByStreamJobId(streamJobId));
    }

    @RequestMapping(value = "{id}/preview", method = RequestMethod.GET)
    public TeslaBaseResult getPreview(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }
        return buildSucceedResult(JsonUtil.map(
        "script", streamJobService.generateScript(streamJobId, job.getOptions().getString("template")),
                "deployment", streamJobService.generateDeploymentByStreamJob(job)
        ));
    }

    @RequestMapping(value = "{id}/operate/start", method = RequestMethod.POST)
    public TeslaBaseResult operateStart(@PathVariable("id") Long streamJobId) throws Exception {
        SreworksStreamJobDTO job = streamJobService.get(streamJobId);
        if(job == null){
            return buildClientErrorResult("streamJob not found");
        }

        // 上传脚本
        JSONObject result = vvpService.uploadArtifact(
                streamJobService.getScriptName(streamJobId),
                streamJobService.generateScript(streamJobId, job.getOptions().getString("template"))
        );

        // 启动flink job


        return buildSucceedResult(result);

//        return buildSucceedResult(JsonUtil.map(
//                "content", streamJobService.generateScript(streamJobId)
//        ));
    }

    @RequestMapping(value = "gets", method = RequestMethod.GET)
    public TeslaBaseResult gets(Integer page, Integer pageSize) throws Exception {
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
           pageSize = 10;
        }
        page = page - 1;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("id").descending());
        Page<SreworksStreamJobDTO> jobList = streamJobService.gets(pageable);
        return buildSucceedResult(JsonUtil.map(
        "page", jobList.getNumber() + 1,
            "pageSize", jobList.getSize(),
            "total", jobList.getTotalElements(),
            "items", jobList.getContent()
        ));

    }

    @RequestMapping(value = "connectors", method = RequestMethod.GET)
    public TeslaBaseResult getConnectors(StreamJobListConnectorParam param) throws Exception {
        List<FlinkConnectorDTO> connectors = vvpService.listConnector(param.getSource(), param.getSink(), param.getType());
        return buildSucceedResult(connectors);
    }

    @RequestMapping(value = "connector/properties", method = RequestMethod.GET)
    public TeslaBaseResult getConnectorProperties(String type, String columns) throws Exception {
        if(!StringUtils.isEmpty(type)){
            List<FlinkConnectorDTO> connectors = vvpService.listConnector(null, null, type);
            if (connectors.size() > 0) {

                // 初始化包含 # 和不包含 # 的 JSONArray 数组
                JSONArray propertiesWithSharp = new JSONArray();
                JSONArray properties = new JSONArray();

                for (int i = 0; i < connectors.get(0).getProperties().size(); i++) {
                    JSONObject obj = connectors.get(0).getProperties().getJSONObject(i);
                    if (obj.getString("key").contains("#")) {
                        propertiesWithSharp.add(obj);
                    } else {
                        obj.put("label", obj.getString("key"));
                        obj.put("value", obj.getString("key"));
                        if (
                            StringUtils.equals("", obj.getString("defaultValue")) &&
                            obj.getString("description") != null &&
                            obj.getString("description").startsWith("Must be set to")
                        ){
                            obj.put("defaultValue", obj.getString("description").split("'")[1]);
                        }
                        properties.add(obj);
                    }
                }

                // 将含有#的配置按照columns拓展开
                if(propertiesWithSharp.size() > 0 && !StringUtils.isEmpty(columns)) {
                    for (String column : columns.split(",")) {
                        for (int i = 0; i < propertiesWithSharp.size(); i++) {
                            JSONObject property = propertiesWithSharp.getJSONObject(i);
                            JSONObject obj = property.clone();
                            String key = obj.getString("key").replace("#", column);
                            obj.put("key", key);
                            obj.put("label", key);
                            obj.put("value", key);
                            properties.add(obj);
                        }
                    }
                }

                return buildSucceedResult(JsonUtil.map("options", properties));
            }
        }
        return buildSucceedResult(null);
    }


}
