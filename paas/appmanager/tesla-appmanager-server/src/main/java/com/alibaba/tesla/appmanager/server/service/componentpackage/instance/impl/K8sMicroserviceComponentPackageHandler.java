package com.alibaba.tesla.appmanager.server.service.componentpackage.instance.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.ImageBuilderProperties;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.autoconfig.SystemProperties;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.CommandUtil;
import com.alibaba.tesla.appmanager.common.util.ExceptionUtil;
import com.alibaba.tesla.appmanager.common.util.FileUtil;
import com.alibaba.tesla.appmanager.common.util.JsonUtil;
import com.alibaba.tesla.appmanager.common.util.KanikoBuildCheckUtil;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.StringUtil;
import com.alibaba.tesla.appmanager.domain.core.StorageFile;
import com.alibaba.tesla.appmanager.domain.dto.ComponentPackageTaskDTO;
import com.alibaba.tesla.appmanager.domain.req.componentpackage.BuildComponentHandlerReq;
import com.alibaba.tesla.appmanager.domain.res.componentpackage.LaunchBuildComponentHandlerRes;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.KanikoBuildPodStateChecker;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.constant.PodStatusPhaseEnum;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.dto.WaitKanikoBuildPod;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.util.BuildUtil;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.util.RetryUtil;
import com.alibaba.tesla.appmanager.server.storage.Storage;

import com.hubspot.jinjava.Jinjava;
import io.jsonwebtoken.lang.Assert;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import okhttp3.OkHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.tomcat.jni.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @ClassName: K8sMicroserviceComponentPackageHandler
 * @Author: dyj
 * @DATE: 2023-05-10
 * @Description:
 **/
@Slf4j
@Component
public class K8sMicroserviceComponentPackageHandler {
    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";
    private static final String VOLUME_PATH = "/app/kaniko/";
    private static final String DEFAULT_MICROSERVICE_TYPE = "Deployment";
    private static final String TEMPLATE_MICROSERVICE_FILENAME = "jinja/default_microservice_%s.tpl";
    private static final String KANIKO_TPL_PATH = "kaniko/kaniko.yaml.tpl";
    private CoreV1Api api;

    @Autowired
    private SystemProperties systemProperties;
    @Autowired
    private ImageBuilderProperties imageBuilderProperties;
    @Autowired
    private PackageProperties packageProperties;
    @Autowired
    private Storage storage;
    @Autowired
    private KanikoBuildPodStateChecker kanikoBuildPodStateChecker;

    @PostConstruct
    public void init() throws IOException {
        ApiClient client = ClientBuilder.defaultClient();
        OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(3, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);
        api = new CoreV1Api(client);
    }

    /**
     * @param componentHandlerReq
     * @return
     */
    public LaunchBuildComponentHandlerRes launch(BuildComponentHandlerReq componentHandlerReq) throws IOException {
        ComponentPackageTaskDTO taskDTO = componentHandlerReq.getTaskDTO();
        long tag = System.currentTimeMillis();
        String relativePath = componentHandlerReq.getAppId() + "/" + componentHandlerReq.getComponentName() + tag + "/";
        String targetFileDir = VOLUME_PATH + relativePath;
        FileUtil.createDir(targetFileDir, true);
        JSONObject packageOptions = componentHandlerReq.getOptions();
        Assert.notNull(packageOptions, "action=launch|| package options can not been null!");

        // 1. 生成imageName
        JSONArray containers = loadContainer(packageOptions);
        for (int index = 0; index < containers.size(); index++) {
            JSONObject container = containers.getJSONObject(index);
            KanikoBuildCheckUtil.buildCheck(container.getJSONObject("build"));
            container.put("image", generateImageName(taskDTO, container, Long.toString(tag)));
        }

        // 2. 生成meta yaml
        String metaYaml = generateMetaYaml(taskDTO, packageOptions, targetFileDir);

        // 3. 渲染构建pod
        String podTpl = loadClassPathFile(KANIKO_TPL_PATH);
        List<V1Pod> waitApplyPodList = new LinkedList<>();
        Set<String> remoteObjectSet = new HashSet<>();
        try {
            for (int index = 0; index < containers.size(); index++) {
                JSONObject container = containers.getJSONObject(index);
                String existImage = JsonUtil.recursiveGetString(container, Arrays.asList("build", "useExistImage"));
                if (!container.getJSONObject("build").getBooleanValue("imagePush") && !StringUtils.isEmpty(existImage)) {
                    log.info("action=renderBuildPod|| container {} have been exist image:{}", container.getString("name"), existImage);
                } else {
                    V1Pod containerBuildPod = renderBuildPod(api, container, podTpl, taskDTO, relativePath, tag, remoteObjectSet);
                    waitApplyPodList.add(containerBuildPod);
                }
            }
        } catch (Exception e) {
            // 清理remoteObject
            log.info("action=launch|| Can not render kaniko pod!");
            cleanRecord(targetFileDir, remoteObjectSet);
            // 继续throw异常
            throw new AppException(AppErrorCode.BUILD_ERROR, "action=launch|| Can not render kaniko pod!", e);
        }

        // 4. apply构建pod
        WaitKanikoBuildPod waitPod = new WaitKanikoBuildPod();
        for (V1Pod v1Pod : waitApplyPodList) {
            waitPod.addRunning( v1Pod.getMetadata().getName());
            try {
                api.createNamespacedPod(systemProperties.getK8sNamespace(), v1Pod, null, null, null);
            } catch (Exception e) {
                log.error("action=createPod|| can not apply pod yaml:{}, Message:{} Exception:{}", v1Pod.toString(), e.getMessage(), e.getCause());
                String logContent = BuildUtil.genLogContent(v1Pod.getMetadata().getName(), ExceptionUtil.getStackTrace(e));
                waitPod.changeStatus(v1Pod.getMetadata().getName(), PodStatusPhaseEnum.Failed, logContent);
            }
        }

        // 5. 轮询pod状态及日志
        kanikoBuildPodStateChecker.add(waitPod);
        while (true) {
            if (waitPod.haveSucceed()) {
                kanikoBuildPodStateChecker.clean(waitPod);
                cleanRecord(targetFileDir, remoteObjectSet);
                String zipFilePath = targetFileDir + "component_package_task.zip";
                ZipFile zip = new ZipFile(zipFilePath);
                ZipParameters zipParameters = new ZipParameters();
                zipParameters.setIncludeRootFolder(false);
                zipParameters.setExcludeFileFilter(file -> {
                    if (file.getName().endsWith(".tar")) {
                        return false;
                    }
                    if ("meta.yaml".equalsIgnoreCase(file.getName())) {
                        return false;
                    }
                    return true;
                });
                try {
                    zip.addFolder(new File(targetFileDir), zipParameters);
                } catch (ZipException e) {
                    log.error("action=packageComponentZip|| fail to addFolder to zip!||message={}, Exception={}", e.getMessage(), e.getCause());
                    throw new AppException(AppErrorCode.USER_CONFIG_ERROR, "actionName=packageComponentZip|| Can not create zip file:" + targetFileDir, e.getMessage(), e.getCause());
                }

                String targetFileMd5 = StringUtil.getMd5Checksum(zipFilePath);

                String bucketName = packageProperties.getBucketName();
                String remotePath = PackageUtil
                    .buildComponentPackageRemotePath(taskDTO.getAppId(), taskDTO.getComponentType(), taskDTO.getComponentName(),
                        taskDTO.getPackageVersion());
                storage.putObject(bucketName, remotePath, zipFilePath);
                StorageFile storageFile = new StorageFile(bucketName, remotePath);
                return LaunchBuildComponentHandlerRes.builder()
                    .logContent(waitPod.getLog())
                    .storageFile(storageFile)
                    .packageMetaYaml(metaYaml)
                    .packageMd5(targetFileMd5)
                    .build();
            } else if (waitPod.haveFailed()) {
                kanikoBuildPodStateChecker.clean(waitPod);
                cleanRecord(targetFileDir, remoteObjectSet);
                throw new AppException(AppErrorCode.BUILD_ERROR, waitPod.getLog());
            }
            Time.sleep(20000);
        }
    }

    private JSONArray loadContainer(JSONObject packageOptions) {
        JSONArray containers = new JSONArray();
        if (packageOptions.containsKey("initContainers")) {
            containers.addAll(packageOptions.getJSONArray("initContainers"));
        }
        if (packageOptions.containsKey("containers")) {
            containers.addAll(packageOptions.getJSONArray("containers"));
        }
        if (containers.isEmpty()) {
            log.warn("The build yaml have no build contains!");
            throw new AppException(AppErrorCode.USER_CONFIG_ERROR, "action=loadContainer|| The microservice build yaml have no build contains!");
        }
        return containers;
    }

    private String generateImageName(ComponentPackageTaskDTO taskDTO, JSONObject container, String tag) {
        String existImage = JsonUtil.recursiveGetString(container, Arrays.asList("build", "useExistImage"));
        if (!container.getJSONObject("build").getBooleanValue("imagePush") && !StringUtils.isEmpty(existImage)) {
            return existImage;
        }
        String imageBaseName = String.format("%s-%s-%s", taskDTO.getAppId(), taskDTO.getComponentName(),
            container.get("name"));
        String image = imageBaseName + ":" + tag;
        String destination = JsonUtil.recursiveGetString(container, Arrays.asList("build", "imagePushRegistry"));
        if (StringUtils.isEmpty(destination)) {
            throw new AppException(AppErrorCode.USER_CONFIG_ERROR,
                "Can not find imagePushRegistry from build.yaml");
        }
        return destination.concat("/").concat(image);
    }

    private String generateMetaYaml(ComponentPackageTaskDTO taskDTO,
        JSONObject packageOptions, String targetFileDir) throws IOException {

        packageOptions.put("appId", taskDTO.getAppId());
        packageOptions.put("version", taskDTO.getPackageVersion());
        packageOptions.put("componentType", taskDTO.getComponentType());
        packageOptions.put("componentName", taskDTO.getComponentName());

        String kind = packageOptions.getString("kind");
        if (StringUtils.isEmpty(kind)) {
            kind = DEFAULT_MICROSERVICE_TYPE;
        }
        String metaTpl = loadClassPathFile(String.format(TEMPLATE_MICROSERVICE_FILENAME, kind));

        String metaYamlContent = jinjaRender(metaTpl, packageOptions);
        FileUtil.writeStringToFile(targetFileDir + "meta.yaml", metaYamlContent, true);
        return metaYamlContent;
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

    private V1Pod renderBuildPod(CoreV1Api api, JSONObject container, String podTpl, ComponentPackageTaskDTO taskDTO,
        String relativePath, long tag, Set<String> remoteObjectSet) throws IOException {
        Object nameOb = JsonUtil.recursiveGetParameter(container,
            Collections.singletonList("name"));
        String containerName = nameOb != null ? nameOb.toString() : "default";
        log.info("action=renderBuildPod || Start render container:{}, build yaml:{}", containerName, container);

        // 渲染 dockerfileTpl
        // 1. 下载 repo
        String gitRepPath = VOLUME_PATH + relativePath + containerName + "/git";
        RetryUtil.getRetryClient().execute(retryContext -> gitClone(gitRepPath, container));

        Object repoPathPara = JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "repoPath"));
        String repoPath = repoPathPara == null ? null : repoPathPara.toString();
        String containerPath = VOLUME_PATH + relativePath + containerName + "/";
        String buildAbsolutePath = gitRepPath + "/";
        if (!StringUtils.isEmpty(repoPath)) {
            if (repoPath.endsWith("/")) {
                buildAbsolutePath = buildAbsolutePath + repoPath;
            } else {
                buildAbsolutePath = buildAbsolutePath + repoPath + "/";
            }
        }
        // 2. 读取dockertemplateFile
        String tplName = JsonUtil.recursiveGetString(container, Arrays.asList("build", "dockerfileTemplate"));
        org.springframework.util.Assert.isTrue(!StringUtils.isEmpty(tplName), "The parameter of build.dockerfileTemplate can not been null!");
        String dockerFileName = tplName;
        if (tplName.endsWith(".tpl")) {
            dockerFileName = tplName.substring(0, tplName.length() - 4);
        }
        if (tplName.contains("/")) {
            dockerFileName = dockerFileName.substring(tplName.lastIndexOf("/")+1);
        }
        File dockerTemplateFile = new File(buildAbsolutePath + tplName);
        if (!dockerTemplateFile.exists()) {
            throw new AppException(AppErrorCode.USER_CONFIG_ERROR,
                "Can not find dockerfileTemplateFile:" + dockerTemplateFile.getAbsolutePath());
        }
        JSONObject dockerTemplateArgs = JSONObject.parseObject(JSONObject.toJSONString(
            JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "dockerfileTemplateArgs"))));
        String dockerTemplateRaw = IOUtils.toString(new FileInputStream(dockerTemplateFile), StandardCharsets.UTF_8);
        String dockerFileStr = jinjaRender(dockerTemplateRaw, dockerTemplateArgs);
        // 3. 生成 dockerfile
        boolean b = FileUtil.writeStringToFile(buildAbsolutePath + dockerFileName, dockerFileStr, true);
        log.info("action=JinjaRender||dockerFileStr:{}", dockerFileStr);
        // 4. 打包成tar.gz （不打包 markdown 文件）
        String compressTarName = dockerFileName + ".tar.gz";
        // String tarCommand = String.format("cd %s; tar zcvf %s -C %s .[!.]* *", buildAbsolutePath, buildAbsolutePath + compressTarName, buildAbsolutePath);
        String[] createCompressTar = new String[]{"touch", containerPath + compressTarName};
        CommandUtil.runLocalCommand(CommandUtil.getBashCommand(createCompressTar), Paths.get(containerPath).toFile());
        String[] tarCommand = new String[]{"tar", "zcvf", containerPath + compressTarName, "."};
        CommandUtil.runLocalCommand(CommandUtil.getBashCommand(tarCommand), Paths.get(buildAbsolutePath).toFile());

        String bucketName = packageProperties.getBucketName();
        String remotePath = PackageUtil
            .buildKanikoBuildRemotePath(taskDTO.getAppId(), taskDTO.getComponentType(), taskDTO.getComponentName(), containerName,
                taskDTO.getPackageVersion());
        storage.putObject(bucketName, remotePath, containerPath + compressTarName);
        remoteObjectSet.add(remotePath);
        StorageFile storageFile = new StorageFile(bucketName, remotePath);
        log.info("kaniko build package has uploaded to storage||componentPackageTaskId={}||bucketName={}||" +
            "remotePath={}||localPath={}", taskDTO.getId(), bucketName, remotePath, containerPath + compressTarName);

        // 5. 渲染 kaniko pod
        JSONObject parameters = new JSONObject();
        parameters.put("CONTEXT", storageFile.toPath());
        parameters.put("DOCKERFILE", dockerFileName);
        String podName = genPodName(taskDTO.getAppId(), taskDTO.getComponentName(), containerName, taskDTO.getId(), tag);
        parameters.put("POD_NAME", podName);
        JSONObject buildArgs = JSONObject.parseObject(JSONObject.toJSONString(
            JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "args"))));
        if (buildArgs != null) {
            JSONArray args = new JSONArray();
            for (String key : buildArgs.keySet()) {
                String arg = String.format("--build-arg=%s=%s", key, buildArgs.getString(key));
                args.add(arg);
            }
            parameters.put("BUILD_ARGS", args);
        }
        JSONArray imageTags = JSONArray.parseArray(JSONObject.toJSONString(
            JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "imagePushTags"))));
        JSONArray destinations = new JSONArray();
        destinations.add(container.getString("image"));
        if (imageTags != null) {
            for (Object imageTag : imageTags) {
                destinations.add(generateImageName(taskDTO, container, imageTag.toString()));
            }
        }
        parameters.put("DESTINATIONS", destinations);
        parameters.put("STORAGE_ACCESS_KEY", packageProperties.getAccessKey());
        parameters.put("STORAGE_SECRET_KEY", packageProperties.getSecretKey());
        parameters.put("STORAGE_ENDPOINT", packageProperties.getEndpointProtocol() + packageProperties.getEndpoint());
        parameters.put("KANIKO_IMAGE", packageProperties.getKanikoImage());
        String snapshotMode = JsonUtil.recursiveGetString(container, Arrays.asList("build", "snapshotMode"));
        if (StringUtils.isEmpty(snapshotMode)) {
            parameters.put("SNAPSHOTMODE", snapshotMode);
        }
        // set docker secret name
        if (StringUtils.isEmpty(JsonUtil.recursiveGetString(container, Arrays.asList("build", "dockerSecretName")))) {
            parameters.put("K8S_DOCKER_SECRET", systemProperties.getK8sDockerSecret());
        } else {
            parameters.put("K8S_DOCKER_SECRET", JsonUtil.recursiveGetString(container, Arrays.asList("build", "dockerSecretName")));
        }
        // 携带flag设定
        JSONArray configKanikoFlags = JSONArray.parseArray(JSONObject.toJSONString(
            JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "configKanikoFlags"))));
        if (configKanikoFlags != null) {
            parameters.put("CONFIG_KANIKO_FLAGS", configKanikoFlags);
        }

        String render = jinjaRender(podTpl, parameters);
        log.info("actionName=buildImage-kaniko||parameters={}\nkaniko_pod.yaml:{}", parameters, render);
        V1Pod v1Pod = SchemaUtil.toSchema(V1Pod.class, render);
        try {
            api.deleteNamespacedPod(podName.toString(), systemProperties.getK8sNamespace(), null,
                null, null, null, null, null);
        } catch (ApiException e) {
            log.info("clean pod");
        }
        return v1Pod;
    }

    private boolean gitClone(String localDir, JSONObject container) {
        FileUtil.createDir(localDir, true);

        Object ciAccountPara = JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "ciAccount"));
        Object ciTokenPara = JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "ciToken"));
        String ciAccount = ciAccountPara == null ? imageBuilderProperties.getDefaultCiAccount() : ciAccountPara.toString();
        String ciToken = ciTokenPara == null ? imageBuilderProperties.getDefaultCiToken() : ciTokenPara.toString();

        Object repoPara = JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "repo"));
        String gitHttpRep;
        if (repoPara == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                "can not found git repo from the container yaml path: build.repo!");
        } else {
            String repo = repoPara.toString();
            if (repo.startsWith(HTTP_PREFIX)) {
                String rest = StringUtil.trimStringByString(repo, HTTP_PREFIX);
                gitHttpRep = String.format("%s%s:%s@%s", HTTP_PREFIX, ciAccount, ciToken, rest);
            } else if (repo.startsWith(HTTPS_PREFIX)) {
                String rest = StringUtil.trimStringByString(repo, HTTPS_PREFIX);
                gitHttpRep = String.format("%s%s:%s@%s", HTTPS_PREFIX, ciAccount, ciToken, rest);
            } else {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot generate authorized repo by string " + repo);
            }
        }
        String branch = String.valueOf(JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "branch")));
        String[] gitCloneCommand = new String[]{"git", "clone", "-b", branch, gitHttpRep, localDir};
        CommandUtil.runLocalCommand(gitCloneCommand);
        Object commit = JsonUtil.recursiveGetParameter(container, Arrays.asList("build", "commit"));
        if (commit != null) {
            String[] resetCommit = new String[]{"git", "reset", "--hard", String.valueOf(commit)};
            CommandUtil.runLocalCommand(resetCommit, Paths.get(localDir).toFile());
        }
        return true;
    }

    private String genPodName(String appId, String componentName, String containerName, Long taskId, long tag) {
        return "appmanager-build-" + appId + "-"
            + componentName + "-"
            + containerName + "-"
            + taskId + "-"
            + tag;
    }

    private void cleanRecord(String targetFileDir, Set<String> remoteObjectSet) {
        deleteDir(targetFileDir);
        deleteRemoteObject(remoteObjectSet);
    }

    private void deleteDir(String targetFileDir) {
        if (StringUtils.isEmpty(targetFileDir)) {
            log.error("action=deleteDir|| dir is empty!");
            return;
        }
        String[] deleteCommand = new String[]{"rm", "-rf", targetFileDir};
        CommandUtil.runLocalCommand(deleteCommand);
    }

    private void deleteRemoteObject(Set<String> remoteObjectSet) {
        if (CollectionUtils.isEmpty(remoteObjectSet)) {
            log.info("action=deleteRemoteObject|| remoteObjectSet is Empty!");
            return;
        }
        for (String ob : remoteObjectSet) {
            storage.removeObject(packageProperties.getBucketName(), ob);
        }
    }
}
