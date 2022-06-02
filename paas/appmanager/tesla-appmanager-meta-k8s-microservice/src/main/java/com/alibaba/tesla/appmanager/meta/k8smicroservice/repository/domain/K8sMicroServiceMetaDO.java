package com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.SystemProperties;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.ContainerTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.dto.*;
import com.alibaba.tesla.dag.common.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum.K8S_JOB;
import static com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum.K8S_MICROSERVICE;

/**
 * k8s微应用元信息
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class K8sMicroServiceMetaDO implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 应用标示
     */
    private String appId;

    /**
     * 微服务标示
     */
    private String microServiceId;

    /**
     * 微服务名称
     */
    private String name;

    /**
     * 微服务描述
     */
    private String description;

    /**
     * 扩展信息
     */
    private String microServiceExt;

    /**
     * options
     */
    private String options;

    /**
     * 组件类型
     */
    private ComponentTypeEnum componentType;

    /**
     * 类型 (Deployment/StatefulSet/CloneSet/AdvancedStatefulSet)
     */
    private String kind;

    /**
     * 环境变量--弹内
     */
    private List<EnvMetaDTO> envList;

    /**
     * 构建对象--弹内
     */
    private List<ContainerObjectDTO> containerObjectList;

    /**
     * 构建对象--开源
     */
    private List<InitContainerDTO> initContainerList;

    /**
     * 环境变量--开源
     */
    private List<String> envKeyList;

    /**
     * 仓库配置--开源
     */
    private RepoDTO repoObject;

    /**
     * 镜像推送
     */
    private ImagePushDTO imagePushObject;

    /**
     * 部署对象
     */
    private LaunchDTO launchObject;

    private static ContainerObjectDTO getContainerObjectDTO(JSONObject container, ContainerTypeEnum containerType) {
        ContainerObjectDTO containerObjectDTO = new ContainerObjectDTO();
        containerObjectDTO.setContainerType(containerType);
        containerObjectDTO.setName(container.getString("name"));

        JSONObject build = container.getJSONObject("build");
        containerObjectDTO.setRepo(build.getString("repo"));
        containerObjectDTO.setBranch(build.getString("branch"));
        containerObjectDTO.setDockerfileTemplate(build.getString("dockerfileTemplate"));

        List<ArgMetaDTO> dockerfileTemplateArgs = new ArrayList<>();
        if (MapUtils.isNotEmpty(build.getJSONObject("dockerfileTemplateArgs"))) {
            build.getJSONObject("dockerfileTemplateArgs").forEach((k, v) ->
                    dockerfileTemplateArgs.add(ArgMetaDTO.builder().name(k).value((String) v).build())
            );
        }

        containerObjectDTO.setDockerfileTemplateArgs(dockerfileTemplateArgs);

        List<ArgMetaDTO> buildArgs = new ArrayList<>();
        if (MapUtils.isNotEmpty(build.getJSONObject("args"))) {
            build.getJSONObject("args").forEach((k, v) -> buildArgs.add(ArgMetaDTO.builder().name(k)
                    .value((String) v).build())
            );
        }

        containerObjectDTO.setBuildArgs(buildArgs);

        List<PortMetaDTO> ports = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(container.getJSONArray("ports"))) {
            container.getJSONArray("ports").forEach(port -> {
                Map<String, Object> arg = (Map) port;
                arg.forEach((key, value) -> ports.add(PortMetaDTO.builder().name(key)
                        .value(String.valueOf(value)).build()));
            });
        }

        containerObjectDTO.setPorts(ports);

        if (CollectionUtils.isNotEmpty(container.getJSONArray("command"))) {
            containerObjectDTO.setCommand(container.getJSONArray("command").toJSONString());
        }

        return containerObjectDTO;
    }

    /**
     * 增加 imagePush 配置
     *
     * @param build 构建对象
     */
    private static void addImagePushProperties(JSONObject build, ImagePushDTO imagePushObject) {
        build.put("imagePush", true);
        if (Objects.nonNull(imagePushObject) && Objects.nonNull(imagePushObject.getImagePushRegistry())) {
            build.put("imagePushRegistry", String.format("%s/%s",
                    imagePushObject.getImagePushRegistry().getDockerRegistry(),
                    imagePushObject.getImagePushRegistry().getDockerNamespace()));
        } else {
            SystemProperties systemProperties = BeanUtil.getBean(SystemProperties.class);
            build.put("imagePushRegistry", String.format("%s/%s",
                    systemProperties.getDockerRegistry(), systemProperties.getDockerNamespace()));
        }
    }

    private static List<ContainerObjectDTO> buildContainerObjectList(String microServiceId, RepoDTO repoDTO,
                                                                     List<InitContainerDTO> initContainerList) {
        List<ContainerObjectDTO> containerObjectDTOList = new ArrayList<>();
        ContainerObjectDTO container =
                ContainerObjectDTO.builder().containerType(ContainerTypeEnum.CONTAINER).appName(microServiceId)
                        .branch(DefaultConstant.DEFAULT_REPO_BRANCH).name(microServiceId).repo(repoDTO.getRepo()).repoDomain(repoDTO
                        .getRepoDomain()).repoGroup(repoDTO.getRepoGroup()).repoType(repoDTO.getRepoType())
                        .ciAccount(repoDTO.getCiAccount()).ciToken(repoDTO.getCiToken()).repoPath(repoDTO.getRepoPath()).build();

        if (StringUtils.isNotEmpty(repoDTO.getDockerfilePath())) {
            container.setDockerfileTemplate(repoDTO.getDockerfilePath());
        } else {
            container.setDockerfileTemplate("Dockerfile.tpl");
        }

        containerObjectDTOList.add(container);
        if (CollectionUtils.isNotEmpty(initContainerList)) {
            for (InitContainerDTO initContainerDTO : initContainerList) {

                ContainerObjectDTO initContainer =
                        ContainerObjectDTO.builder().containerType(ContainerTypeEnum.INIT_CONTAINER).appName
                                (microServiceId)
                                .branch("master").name(initContainerDTO.createContainerName()).repo(repoDTO.getRepo()).repoDomain
                                (repoDTO.getRepoDomain()).repoGroup(repoDTO.getRepoGroup()).repoType(repoDTO.getRepoType())
                                .ciAccount(repoDTO.getCiAccount()).ciToken(repoDTO.getCiToken()).repoPath(repoDTO.getRepoPath()).build();

                if (StringUtils.isNotEmpty(initContainerDTO.getDockerfilePath())) {
                    initContainer.setDockerfileTemplate(initContainerDTO.getDockerfilePath());
                } else {
                    initContainer.setDockerfileTemplate(initContainerDTO.createDockerFileTemplate());
                }

                containerObjectDTOList.add(initContainer);
            }
        }

        return containerObjectDTOList;
    }

    public static String createOption(
            ComponentTypeEnum componentType, String kind, List<EnvMetaDTO> envList,
            List<ContainerObjectDTO> containerObjectDTOList, ImagePushDTO imagePushObject) {
        JSONObject options = new JSONObject();
        options.put("kind", kind);
        JSONArray env = new JSONArray();
        if (CollectionUtils.isNotEmpty(envList)) {
            envList.forEach(envDTO -> {
                if(StringUtils.isNotBlank(envDTO.getName())){
                    env.add(envDTO.getName().split("=")[0]);
                }
            });
        }
        options.put("env", env);

        if (componentType == K8S_MICROSERVICE || componentType == K8S_JOB) {
            createK8sService(componentType, containerObjectDTOList, options, imagePushObject);
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid component type");
        }

        JSONObject result = new JSONObject();
        result.put("options", options);

        Yaml yaml = SchemaUtil.createYaml(JSONObject.class);
        return yaml.dumpAsMap(result);
    }

    private static void createK8sService(
            ComponentTypeEnum componentType, List<ContainerObjectDTO> containerObjectDTOList, JSONObject options,
            ImagePushDTO imagePushObject) {
        JSONArray initContainers = new JSONArray();
        JSONArray containers = new JSONArray();
        JSONObject job = new JSONObject();

        containerObjectDTOList.forEach(containerObjectDTO -> {
            JSONObject build = new JSONObject();

            build.put("repo", containerObjectDTO.getRepo());
            build.put("branch", containerObjectDTO.getBranch());
            build.put("dockerfileTemplate", containerObjectDTO.getDockerfileTemplate());
            build.put("ciAccount", containerObjectDTO.getCiAccount());
            build.put("ciToken", containerObjectDTO.getCiToken());
            if (StringUtils.isNotEmpty(containerObjectDTO.getRepoPath())) {
                build.put("repoPath", containerObjectDTO.getRepoPath());
            }
            addImagePushProperties(build, imagePushObject);

            if (Objects.nonNull(imagePushObject) && StringUtils.isNotEmpty(imagePushObject.getDockerSecretName())) {
                build.put("dockerSecretName", imagePushObject.getDockerSecretName());
            }

            JSONObject dockerfileTemplateArgs = new JSONObject();
            if (CollectionUtils.isNotEmpty(containerObjectDTO.getDockerfileTemplateArgs())) {
                containerObjectDTO.getDockerfileTemplateArgs().forEach(
                        argDTO -> dockerfileTemplateArgs.put(argDTO.getName(), argDTO.getValue())
                );
            }
            build.put("dockerfileTemplateArgs", dockerfileTemplateArgs);

            JSONObject buildArgs = new JSONObject();
            if (CollectionUtils.isNotEmpty(containerObjectDTO.getBuildArgs())) {
                containerObjectDTO.getBuildArgs().forEach(
                        argDTO -> buildArgs.put(argDTO.getName(), argDTO.getValue()));
            }

            build.put("args", buildArgs);
            JSONObject container = new JSONObject();
            container.put("name", containerObjectDTO.getName());
            container.put("build", build);

            if (CollectionUtils.isNotEmpty(containerObjectDTO.getPorts())) {
                JSONArray ports = new JSONArray();
                containerObjectDTO.getPorts().forEach(portDTO -> {
                    JSONObject args = new JSONObject();
                    args.put(portDTO.getName(), portDTO.getValue());
                    ports.add(args);
                });

                container.put("ports", ports);
            }

            if (StringUtils.isNotEmpty(containerObjectDTO.getCommand())) {
                if (StringUtils.isEmpty(containerObjectDTO.getCommand())) {
                    container.put("command", new JSONArray());
                } else {
                    JSONArray commandArray = JSONArray.parseArray(JSONArray.toJSONString(
                            containerObjectDTO.getCommand().split("\\s+")));
                    container.put("command", commandArray);
                }
            }

            if (componentType == K8S_JOB) {
                job.putAll(container);
                options.put("job", job);
            } else {
                if (Objects.nonNull(containerObjectDTO.getContainerType())) {
                    switch (containerObjectDTO.getContainerType()) {
                        case INIT_CONTAINER:
                            initContainers.add(container);
                            if (CollectionUtils.isNotEmpty(initContainers)) {
                                options.put("initContainers", initContainers);
                            }
                            break;
                        case CONTAINER:
                            containers.add(container);
                            if (CollectionUtils.isNotEmpty(containers)) {
                                options.put("containers", containers);
                            }
                            break;
                    }
                }
            }
        });
    }

    public void init() {
        List<EnvMetaDTO> tempEnvList = envList;
        List<ContainerObjectDTO> tempContainerObjectList = containerObjectList;

        JSONObject microServiceExtJson = new JSONObject();

        if (StringUtils.isNotEmpty(kind)) {
            microServiceExtJson.put("kind", kind);
        } else {
            microServiceExtJson.put("kind", "AdvancedStatefulSet");
        }

        if (CollectionUtils.isNotEmpty(envList)) {
            microServiceExtJson.put("envList", envList);
        }

        if (CollectionUtils.isNotEmpty(containerObjectList)) {
            microServiceExtJson.put("containerObjectList", containerObjectList);
        }

        if (CollectionUtils.isNotEmpty(envKeyList)) {
            microServiceExtJson.put("envKeyList", envKeyList);
            tempEnvList = envKeyList.stream()
                    .map(envKey -> EnvMetaDTO.builder()
                            .name(envKey)
                            .build())
                    .collect(Collectors.toList());
        }

        if (CollectionUtils.isNotEmpty(initContainerList)) {
            microServiceExtJson.put("initContainerList", initContainerList);
        }

        if (Objects.nonNull(repoObject)) {
            microServiceExtJson.put("repo", repoObject);
            tempContainerObjectList = buildContainerObjectList(microServiceId, repoObject, initContainerList);
        }

        if (Objects.nonNull(imagePushObject)) {
            microServiceExtJson.put("image", imagePushObject);
        }

        if (Objects.nonNull(launchObject)){
            microServiceExtJson.put("launch", launchObject);
        }


        microServiceExt = microServiceExtJson.toJSONString();

        options = createOption(componentType, kind, tempEnvList, tempContainerObjectList, imagePushObject);
    }

    public void repoFromString() {
        JSONObject microServiceExtJson = JSON.parseObject(microServiceExt);
        if(Objects.isNull(microServiceExtJson)){
            microServiceExtJson = new JSONObject();
        }

        String repoString = microServiceExtJson.getString("repo");
        if (StringUtils.isNotEmpty(repoString)) {
            repoObject = JSON.parseObject(repoString, RepoDTO.class);
        }

        String launchObjectString = microServiceExtJson.getString("launch");
        if (StringUtils.isNotEmpty(launchObjectString)) {
            launchObject = JSON.parseObject(launchObjectString, LaunchDTO.class);
        }
    }

    public void extFromString() {
        JSONObject microServiceExtJson = JSON.parseObject(microServiceExt);
        if(Objects.isNull(microServiceExtJson)){
            microServiceExtJson = new JSONObject();
        }

        kind = microServiceExtJson.getString("kind");

        String envListString = microServiceExtJson.getString("envList");
        if (StringUtils.isNotEmpty(envListString)) {
            envList = JSON.parseArray(envListString, EnvMetaDTO.class);
        } else {
            envList = Collections.emptyList();
        }

        String buildObjectListString = microServiceExtJson.getString("containerObjectList");
        if (StringUtils.isNotEmpty(buildObjectListString)) {
            containerObjectList = JSON.parseArray(buildObjectListString, ContainerObjectDTO.class);
            containerObjectList.forEach(ContainerObjectDTO::initRepo);
        } else {
            containerObjectList = Collections.emptyList();
        }

        String envKeyListString = microServiceExtJson.getString("envKeyList");
        if (StringUtils.isNotEmpty(envKeyListString)) {
            envKeyList = JSON.parseArray(envKeyListString, String.class);
        } else {
            envKeyList = Collections.emptyList();
        }

        String initContainerListString = microServiceExtJson.getString("initContainerList");
        if (StringUtils.isNotEmpty(initContainerListString)) {
            initContainerList = JSON.parseArray(initContainerListString, InitContainerDTO.class);
        } else {
            initContainerList = Collections.emptyList();
        }

        String repoString = microServiceExtJson.getString("repo");
        if (StringUtils.isNotEmpty(repoString)) {
            repoObject = JSON.parseObject(repoString, RepoDTO.class);
        }

        String imagePushString = microServiceExtJson.getString("imagePush");
        if (StringUtils.isNotEmpty(imagePushString)) {
            imagePushObject = JSON.parseObject(imagePushString, ImagePushDTO.class);
        }

        String launchObjectString = microServiceExtJson.getString("launch");
        if (StringUtils.isNotEmpty(launchObjectString)) {
            launchObject = JSON.parseObject(launchObjectString, LaunchDTO.class);
        }
    }

    public void fromOptions(JSONObject options) {
        JSONArray envArray = options.getJSONArray("env");

        List<EnvMetaDTO> envList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(envArray)) {
            envArray.forEach(envObject -> envList.add(EnvMetaDTO.builder().name(envObject.toString()).build()));
        }

        this.envList = envList;

        List<ContainerObjectDTO> containerObjectList = new ArrayList<>();
        JSONArray initContainers = options.getJSONArray("initContainers");
        if (CollectionUtils.isNotEmpty(initContainers)) {
            for (int i = 0; i < initContainers.size(); i++) {
                containerObjectList.add(
                        getContainerObjectDTO(initContainers.getJSONObject(i), ContainerTypeEnum.INIT_CONTAINER));
            }
        }

        JSONArray containers = options.getJSONArray("containers");
        if (CollectionUtils.isNotEmpty(containers)) {
            for (int i = 0; i < containers.size(); i++) {
                containerObjectList.add(
                        getContainerObjectDTO(containers.getJSONObject(i), ContainerTypeEnum.CONTAINER));
            }
        }

        JSONObject job = options.getJSONObject("job");
        if (job != null) {
            containerObjectList.add(getContainerObjectDTO(job, ContainerTypeEnum.K8S_JOB));
        }

        this.containerObjectList = containerObjectList;
    }

    public JSONObject getOptionsAndReplaceBranch(String branch, List<EnvMetaDTO> appEnvList) {
        Yaml yaml = SchemaUtil.createYaml(JSONObject.class);
        JSONObject root = yaml.loadAs(options, JSONObject.class);
        JSONObject optionsJson = root.getJSONObject("options");
        optionsJson.putIfAbsent("kind", "AdvancedStatefulSet");

        JSONArray compEnvList = optionsJson.getJSONArray("env");

        List<String> allEnvList = appEnvList.stream().map(EnvMetaDTO::getName).collect(Collectors.toList());
        for (int i = 0; i < compEnvList.size(); i++) {
            allEnvList.add(compEnvList.getString(i));
        }

        List<String> allDistinctEnvList = allEnvList.parallelStream().distinct().collect(Collectors.toList());
        optionsJson.put("env", allDistinctEnvList);

        switch (componentType) {
            case K8S_JOB:
                JSONObject job = optionsJson.getJSONObject("job");
                if (job != null) {
                    JSONObject build = job.getJSONObject("build");
                    if (StringUtils.isNotEmpty(branch)) {
                        build.put("branch", branch);
                    }

                    if (!build.containsKey("imagePush")) {
                        addImagePushProperties(build, null);
                    }
                }
                break;
            case K8S_MICROSERVICE:
                JSONArray containers = optionsJson.getJSONArray("containers");
                if (CollectionUtils.isNotEmpty(containers)) {
                    for (int i = 0; i < containers.size(); i++) {
                        JSONObject container = containers.getJSONObject(i);
                        JSONObject build = container.getJSONObject("build");
                        if (StringUtils.isNotEmpty(branch)) {
                            build.put("branch", branch);
                        }
                        if (!build.containsKey("imagePush")) {
                            addImagePushProperties(build, null);
                        }
                    }
                }

                JSONArray initContainers = optionsJson.getJSONArray("initContainers");
                if (CollectionUtils.isNotEmpty(initContainers)) {
                    for (int i = 0; i < initContainers.size(); i++) {
                        JSONObject initContainer = initContainers.getJSONObject(i);
                        JSONObject build = initContainer.getJSONObject("build");
                        if (StringUtils.isNotEmpty(branch)) {
                            build.put("branch", branch);
                        }
                        if (!build.containsKey("imagePush")) {
                            addImagePushProperties(build, null);
                        }
                    }
                }
                break;
            default:
                break;
        }
        return optionsJson;
    }
}