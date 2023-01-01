package com.alibaba.tesla.appmanager.deployconfig.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.ProductReleaseProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.common.util.EnvUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigHistoryRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigHistoryQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigHistoryDO;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigEnvId;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.ProductDTO;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.*;
import com.alibaba.tesla.appmanager.domain.req.git.GitCloneReq;
import com.alibaba.tesla.appmanager.domain.req.git.GitUpdateFileReq;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigApplyTemplateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigSyncToGitBaselineRes;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.dag.common.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部署配置服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class DeployConfigServiceImpl implements DeployConfigService {

    private final DeployConfigRepository deployConfigRepository;
    private final DeployConfigHistoryRepository deployConfigHistoryRepository;
    private final GitService gitService;

    public DeployConfigServiceImpl(
            DeployConfigRepository deployConfigRepository,
            DeployConfigHistoryRepository deployConfigHistoryRepository,
            GitService gitService) {
        this.deployConfigRepository = deployConfigRepository;
        this.deployConfigHistoryRepository = deployConfigHistoryRepository;
        this.gitService = gitService;
    }

    /**
     * 在指定条件下是否存在 Type:envBinding 项
     *
     * @param req 检查请求
     * @return true or false
     */
    @Override
    public boolean hasEnvBinding(DeployConfigHasEnvBindingReq req) {
        return deployConfigRepository.selectByCondition(DeployConfigQueryCondition.builder()
                .apiVersion(req.getApiVersion())
                .appId(req.getAppId())
                .isolateNamespaceId(req.getIsolateNamespaceId())
                .isolateStageId(req.getIsolateStageId())
                .typeId((new DeployConfigTypeId(DeployConfigTypeId.TYPE_ENV_BINDING)).toString())
                .enabled(true)
                .build()
        ).size() > 0;
    }

    /**
     * 应用部署模板 (拆分 launch yaml 并分别应用保存)
     *
     * @param req 应用请求
     * @return 应用结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeployConfigApplyTemplateRes<DeployConfigDO> applyTemplate(DeployConfigApplyTemplateReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        String isolateNamespace = req.getIsolateNamespaceId();
        String isolateStage = req.getIsolateStageId();
        String envId = req.getEnvId();
        String config = req.getConfig();
        boolean enabled = req.isEnabled();
        String productId = req.getProductId();
        String releaseId = req.getReleaseId();
        if (StringUtils.isAnyEmpty(apiVersion, config)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "invalid apply template request, apiVersion/config are required");
        }

        DeployAppSchema schema = SchemaUtil.toSchema(DeployAppSchema.class, config);
        List<DeployConfigDO> items = new ArrayList<>();

        // 保存 parameterValues 配置
        String parameterTypeId = new DeployConfigTypeId(DeployConfigTypeId.TYPE_PARAMETER_VALUES).toString();
        items.add(applySingleConfig(apiVersion, appId, parameterTypeId, envId,
                SchemaUtil.toYamlStr(schema.getSpec().getParameterValues(), DeployAppSchema.ParameterValue.class),
                enabled, false, isolateNamespace, isolateStage, productId, releaseId));
        // 保存 components 配置
        for (DeployAppSchema.SpecComponent component : schema.getSpec().getComponents()) {
            DeployAppRevisionName revision = DeployAppRevisionName.valueOf(component.getRevisionName());
            String componentTypeId = new DeployConfigTypeId(
                    revision.getComponentType(), revision.getComponentName()).toString();
            items.add(applySingleConfig(apiVersion, appId, componentTypeId, envId,
                    SchemaUtil.toYamlMapStr(component), enabled, false, isolateNamespace, isolateStage,
                    productId, releaseId));
        }
        // 保存 policies 配置
        String policyTypeId = new DeployConfigTypeId(DeployConfigTypeId.TYPE_POLICIES).toString();
        items.add(applySingleConfig(apiVersion, appId, policyTypeId, envId,
                SchemaUtil.toYamlStr(schema.getSpec().getPolicies(), DeployAppSchema.Policy.class),
                enabled, false, isolateNamespace, isolateStage, productId, releaseId));
        // 保存 workflow 配置
        String workflowTypeId = new DeployConfigTypeId(DeployConfigTypeId.TYPE_WORKFLOW).toString();
        items.add(applySingleConfig(apiVersion, appId, workflowTypeId, envId,
                SchemaUtil.toYamlStr(schema.getSpec().getWorkflow(), DeployAppSchema.Workflow.class),
                enabled, false, isolateNamespace, isolateStage, productId, releaseId));
        return DeployConfigApplyTemplateRes.<DeployConfigDO>builder().items(items).build();
    }

    /**
     * 根据指定查询条件获取列表
     *
     * @param condition 查询请求
     * @return 部署配置列表
     */
    @Override
    public List<DeployConfigDO> list(DeployConfigQueryCondition condition) {
        return deployConfigRepository.selectByCondition(condition);
    }

    /**
     * 根据指定查询条件获取对应配置记录（支持继承）
     *
     * @param condition 查询请求
     * @return 部署配置列表
     */
    @Override
    public DeployConfigDO getWithInherit(DeployConfigQueryCondition condition) {
        if (StringUtils.isAnyEmpty(condition.getApiVersion(), condition.getTypeId())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "invalid getWithInherit parameters, apiVersion/typeId are required");
        }

        List<DeployConfigDO> records = deployConfigRepository.selectByCondition(condition);
        if (records.size() == 0) {
            return null;
        } else if (records.size() > 1) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("multiple deploy config records found, abort|condition=%s",
                            JSONObject.toJSONString(condition)));
        }
        DeployConfigDO record = records.get(0);

        // 如果全局配置，但记录仍然存在继承配置项，则认为系统错误，避免崩溃
        if (record.getInherit() && StringUtils.isEmpty(condition.getAppId())) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("inherit flag and empty appId found at the same time|condition=%s",
                            JSONObject.toJSONString(condition)));
        }

        // 非继承直接返回
        if (record.getInherit() == null || !record.getInherit()) {
            return record;
        }

        // 继承则继续向上获取
        return getWithInherit(DeployConfigQueryCondition.builder()
                .apiVersion(condition.getApiVersion())
                .appId("")
                .typeId(condition.getTypeId())
                .envId(condition.getEnvId())
                .enabled(condition.getEnabled())
                .isolateNamespaceId(condition.getIsolateNamespaceId())
                .isolateStageId(condition.getIsolateStageId())
                .build());
    }

    /**
     * 更新指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 更新请求
     * @return 更新后的对象
     */
    @Override
    public DeployConfigDO update(DeployConfigUpsertReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        String isolateNamespaceId = req.getIsolateNamespaceId();
        String isolateStageId = req.getIsolateStageId();
        String envId = req.getEnvId();
        String typeId = req.getTypeId();
        String config = req.getConfig();
        JSONArray configJsonArray = req.getConfigJsonArray();
        if (configJsonArray != null) {
            config = SchemaUtil.toYamlStr(configJsonArray, JSONArray.class);
        }
        JSONObject configJsonObject = req.getConfigJsonObject();
        if (configJsonObject != null) {
            config = SchemaUtil.toYamlStr(configJsonObject, JSONObject.class);
        }
        boolean inherit = req.isInherit();
        String productId = req.getProductId();
        String releaseId = req.getReleaseId();
        if (StringUtils.isAnyEmpty(apiVersion, appId, typeId)
                || (StringUtils.isEmpty(config) && !inherit && StringUtils.isAnyEmpty(productId, releaseId))) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid deploy config update request|request=%s", JSONObject.toJSONString(req)));
        }

        return applySingleConfig(apiVersion, appId, typeId, envId, config, true, inherit,
                isolateNamespaceId, isolateStageId, productId, releaseId);
    }

    /**
     * 删除指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 删除请求
     */
    @Override
    public void delete(DeployConfigDeleteReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        String isolateNamespaceId = req.getIsolateNamespaceId();
        String isolateStageId = req.getIsolateStageId();
        String envId = req.getEnvId();
        String typeId = req.getTypeId();
        if (StringUtils.isAnyEmpty(apiVersion, typeId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid deploy config delete request|request=%s", JSONObject.toJSONString(req)));
        }

        deleteSingleConfig(apiVersion, appId, typeId, envId, isolateNamespaceId, isolateStageId);
    }

    /**
     * 生成指定应用在指定部署参数下的 Application Configuration Yaml
     *
     * @param req 部署参数
     * @return 生成 Yaml 结果
     */
    @Override
    public DeployConfigGenerateRes generate(DeployConfigGenerateReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        if (StringUtils.isAnyEmpty(apiVersion, appId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid generate request|request=%s", JSONObject.toJSONString(req)));
        }
        DeployAppSchema schema = generateByConfig(req);

        // 禁用 component 配置获取的时候，直接清零
        if (req.isDisableComponentFetching()) {
            schema.getSpec().setComponents(new ArrayList<>());
        }

        return DeployConfigGenerateRes.builder()
                .schema(schema)
                .build();
    }

    /**
     * 将当前的 Application 同步到指定仓库基线中
     *
     * @param req 当前 Application 及目标仓库路径
     */
    @Override
    public DeployConfigSyncToGitBaselineRes syncToGitBaseline(DeployConfigSyncToGitBaselineReq req) {
        // clone 远端基线到本地
        DeployAppSchema remoteConfig = new DeployAppSchema();
        DeployAppSchema config = req.getConfiguration();
        String appId = config.getMetadata().getAnnotations().getAppId();
        StringBuilder logContent = new StringBuilder();
        Path cloneDir;
        try {
            cloneDir = Files.createTempDirectory("appmanager_sync_to_git_baseline_clone_");
        } catch (IOException e) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "cannot create temp directory", e);
        }
        try {
            gitService.cloneRepo(logContent, GitCloneReq.builder()
                    .repo(req.getRepo())
                    .branch(req.getBranch())
                    .ciAccount(req.getCiAccount())
                    .ciToken(req.getCiToken())
                    .keepGitFiles(true)
                    .build(), cloneDir);
            log.info("git baseline has cloned|appId={}|repo={}|branch={}|filePath={}",
                    appId, req.getRepo(), req.getBranch(), req.getFilePath());

            // 解析仓库中的指定文件到 DeployAppSchema
            Path filePath = Paths.get(cloneDir.toString(), req.getFilePath());
            String remoteConfigStr = "";
            if (Files.exists(filePath)) {
                try {
                    remoteConfigStr = Files.readString(filePath);
                    remoteConfig = SchemaUtil.toSchema(DeployAppSchema.class, remoteConfigStr);
                } catch (Exception e) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("cannot parse remote application in git repo|appId=%s|repo=%s|branch=%s|" +
                                            "filePath=%s|exception=%s", appId, req.getRepo(), req.getBranch(),
                                    filePath, ExceptionUtils.getStackTrace(e)));
                }
            }

            // 根据远端配置更新当前配置
            remoteConfig.setMetadata(null);
            String originRemoteConfigStr = SchemaUtil.toYamlMapStr(remoteConfig);
            remoteConfig.copyFrom(config);
            remoteConfigStr = SchemaUtil.toYamlMapStr(remoteConfig);
            if (StringUtils.compare(originRemoteConfigStr, remoteConfigStr) != 0) {
                log.info("current file is different than remote baseline file|appId={}|repo={}|branch={}|filePath={}|" +
                                "originRemoteConfigStr={}|remoteConfigStr={}", appId, req.getRepo(), req.getBranch(), filePath,
                        originRemoteConfigStr, remoteConfigStr);
                gitService.updateFile(logContent, GitUpdateFileReq.builder()
                        .appId(appId)
                        .cloneDir(cloneDir)
                        .gitUserName(req.getCiAccount())
                        .gitUserEmail(String.format("%s@alibaba-inc.com", req.getCiAccount()))
                        .gitRemoteBranch(req.getBranch())
                        .filePath(req.getFilePath())
                        .fileContent(remoteConfigStr)
                        .operator(req.getOperator())
                        .build());
                log.info("remote config yaml has updated in git braseline|appId={}|repo={}|branch={}|filePath={}|" +
                        "content={}", appId, req.getRepo(), req.getBranch(), req.getFilePath(), remoteConfigStr);
            } else {
                log.info("no need to update remote config yaml in git braseline|appId={}|repo={}|branch={}|filePath={}",
                        appId, req.getRepo(), req.getBranch(), req.getFilePath());
            }
        } finally {
            try {
                FileUtils.forceDelete(cloneDir.toFile());
            } catch (Exception e) {
                throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                        String.format("cannot delete clone dir %s|exception=%s",
                                cloneDir.toString(), ExceptionUtils.getStackTrace(e)));
            }
        }
        return DeployConfigSyncToGitBaselineRes.builder()
                .schema(remoteConfig)
                .build();
    }

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req      加入环境请求
     * @param product  产品对象
     * @param filePath 目标路径
     */
    @Override
    public void bindEnvironment(AppEnvironmentBindReq req, ProductDTO product, String filePath) {
        String envId = EnvUtil.generate(req.getUnitId(), req.getClusterId(), req.getNamespaceId(), req.getStageId());

        if (!req.isSafeMode()) {
            // 获取当前的基线层面的 Application Configuration
            DeployAppSchema configuration = getDefaultTemplate(DeployConfigGetDefaultTemplateReq.builder()
                    .apiVersion(req.getApiVersion())
                    .isolateNamespaceId(req.getIsolateNamespaceId())
                    .isolateStageId(req.getIsolateStageId())
                    .unitId(req.getUnitId())
                    .clusterId(req.getClusterId())
                    .namespaceId(req.getNamespaceId())
                    .stageId(req.getStageId())
                    .appId(req.getAppId())
                    .appComponents(req.getAppComponents())
                    .build());
            log.info("current configuration has generated based on current baseline|appId={}|isolateNamespaceId={}|" +
                            "isolateStageId={}|envId={}|operator={}|res={}", req.getAppId(), req.getIsolateNamespaceId(),
                    req.getIsolateStageId(), envId, req.getOperator(), SchemaUtil.toYamlMapStr(configuration));

            // 同步配置文件到远端 Git 仓库文件
            DeployConfigSyncToGitBaselineRes syncRes = syncToGitBaseline(DeployConfigSyncToGitBaselineReq.builder()
                    .configuration(configuration)
                    .repo(product.getBaselineGitAddress())
                    .ciAccount(product.getBaselineGitUser())
                    .ciToken(product.getBaselineGitToken())
                    .branch(req.getBaselineBranch())
                    .filePath(filePath)
                    .operator(req.getOperator())
                    .build());
            log.info("configuration has synced to remote git baseline|productId={}|releaseId={}|appId={}|" +
                            "isolateNamespaceId={}|isolateStageId={}|baselineBranch={}|repo={}|filePath={}|envId={}|" +
                            "operator={}|res={}", req.getProductId(), req.getReleaseId(), req.getAppId(),
                    req.getIsolateNamespaceId(), req.getIsolateStageId(),
                    req.getBaselineBranch(), product.getBaselineGitAddress(), filePath, envId, req.getOperator(),
                    SchemaUtil.toYamlMapStr(syncRes.getSchema()));
        }

        // 写入 Type:envBinding 的类型记录
        DeployConfigTypeId typeId = new DeployConfigTypeId(DeployConfigTypeId.TYPE_ENV_BINDING);
        applySingleConfig(req.getApiVersion(), req.getAppId(), typeId.toString(), envId, "", true, false,
                req.getIsolateNamespaceId(), req.getIsolateStageId(), req.getProductId(), req.getReleaseId());
    }

    /**
     * 获取当前的指定隔离环境下的全局模板清单
     *
     * @param apiVersion         API 版本
     * @param envId              目标环境 ID
     * @param isolateNamespaceId 隔离 Namespace ID
     * @param isolateStageId     隔离 Stage ID
     * @return Deploy Config 记录列表 (如果当前全局模板不合法则抛出异常)
     */
    @Override
    public List<DeployConfigDO> getGlobalTemplate(
            String apiVersion, String envId, String isolateNamespaceId, String isolateStageId) {
        DeployConfigQueryCondition condition = DeployConfigQueryCondition.builder()
                .apiVersion(apiVersion)
                .appId("")
                .enabled(true)
                .envId(envId)
                .isolateNamespaceId(isolateNamespaceId)
                .isolateStageId(isolateStageId)
                .build();
        List<DeployConfigDO> records = deployConfigRepository.selectByCondition(condition);
        for (DeployConfigDO record : records) {
            if (record.getInherit()) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("inherit is forbidden in global template|record=%s",
                                JSONObject.toJSONString(record)));
            }
        }
        String conflictTypeId = getConflictGlobalTemplateTypeId(records);
        if (StringUtils.isNotEmpty(conflictTypeId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("conflict global template in deploy config|envId=%s|isolateNamespaceId=%s|" +
                            "isolateStageId=%s|typeId=%s", envId, isolateNamespaceId, isolateStageId, conflictTypeId));
        }
        return records;
    }

    /**
     * 获取指定应用在指定环境中的默认 Application Configuration 模板
     *
     * @param req 请求参数 (全部参数必填项)
     * @return Application Configuration
     */
    @Override
    public DeployAppSchema getDefaultTemplate(DeployConfigGetDefaultTemplateReq req) {
        // 以下变量用于生成模板
        String appId = req.getAppId();
        String unitId = req.getUnitId();
        String clusterId = req.getClusterId();
        String namespaceId = req.getNamespaceId();
        String stageId = req.getStageId();
        // 以下变量用于定位全局模板
        String apiVersion = req.getApiVersion();
        String envId = EnvUtil.generate(unitId, clusterId, namespaceId, stageId);
        String isolateNamespaceId = req.getIsolateNamespaceId();
        String isolateStageId = req.getIsolateStageId();
        List<AppComponentLocationContainer> appComponentContainers = req.getAppComponents();
        if (StringUtils.isAnyEmpty(apiVersion, isolateNamespaceId, isolateStageId, appId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "apiVersion/envId/isolateNamespaceId/" +
                    "isolateStageId/appId are required");
        }
        boolean multipleCluster = StringUtils.isEmpty(clusterId);

        // 根据当前的全局模板生成 Application Configuration
        DeployConfigGenerateReq deployConfigGenerateRequest = DeployConfigGenerateReq.builder()
                .apiVersion(apiVersion)
                .appId(appId)
                .unitId(unitId)
                .clusterId(clusterId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .build();
        DeployAppSchema schema = emptyDeployAppSchema(deployConfigGenerateRequest);
        List<DeployConfigDO> records = getGlobalTemplate(apiVersion, envId, isolateNamespaceId, isolateStageId);
        log.info("find global templates in system|apiVersion={}|envId={}|isolateNamespaceId={}|isolateStageId={}|" +
                "records={}", apiVersion, envId, isolateNamespaceId, isolateStageId, JSONArray.toJSONString(records));

        // 根据全局模板组装生成当前的应用级的默认模板
        // step==0 非 traits 类型组装, step==1 traits 类型组装 (step==1 依赖 step==0 完成)
        for (int step = 0; step < 2; step++) {
            for (DeployConfigDO current : records) {
                String typeId = current.getTypeId();
                DeployConfigTypeId typeIdObj = DeployConfigTypeId.valueOf(typeId);
                if (step == 0 && DeployConfigTypeId.TYPE_TRAITS.equals(typeIdObj.getType())) {
                    continue;
                } else if (step == 1 && !DeployConfigTypeId.TYPE_TRAITS.equals(typeIdObj.getType())) {
                    continue;
                }

                String config = current.getConfig();
                switch (typeIdObj.getType()) {
                    case DeployConfigTypeId.TYPE_PARAMETER_VALUES: {
                        if (StringUtils.isEmpty(config)) {
                            schema.getSpec().setParameterValues(new ArrayList<>());
                        } else {
                            schema.getSpec().setParameterValues(
                                    SchemaUtil.toSchemaList(DeployAppSchema.ParameterValue.class, config));
                        }
                        break;
                    }
                    case DeployConfigTypeId.TYPE_COMPONENTS: {
                        String componentType = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_TYPE);
                        String componentName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_NAME);
                        if (ComponentTypeEnum.isSystemComponentType(componentType)) {
                            // 系统内置类型组件，只需要对自身进行配置加载
                            if (StringUtils.isEmpty(componentName)) {
                                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                        String.format("componentName is required in current component type %s",
                                                componentType));
                            }

                            DeployAppSchema.SpecComponent component;
                            if (StringUtils.isNotEmpty(config)) {
                                component = SchemaUtil.toSchema(DeployAppSchema.SpecComponent.class, config);
                            } else {
                                component = new DeployAppSchema.SpecComponent();
                            }
                            component.setIdentifier(componentType, componentName);
                            if (!multipleCluster || ComponentTypeEnum.isSystemComponentType(componentType)) {
                                component = enrichComponentScopes(deployConfigGenerateRequest, component);
                            }
                            schema.getSpec().getComponents().add(component);
                        } else {
                            // 用户定义类型组件，需要对当前组件的所有实例进行扫描，并扩展到所有对象
                            if (StringUtils.isNotEmpty(componentName)) {
                                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                        String.format("componentName is not allowed in current component type %s",
                                                componentType));
                            }

                            for (AppComponentLocationContainer container : appComponentContainers) {
                                if (!componentType.equals(container.getComponentType())) {
                                    continue;
                                }

                                DeployAppSchema.SpecComponent component;
                                if (StringUtils.isNotEmpty(config)) {
                                    component = SchemaUtil.toSchema(DeployAppSchema.SpecComponent.class, config);
                                } else {
                                    component = new DeployAppSchema.SpecComponent();
                                }
                                component.setIdentifier(container.getComponentType(), container.getComponentName());
                                if (!multipleCluster) {
                                    component = enrichComponentScopes(deployConfigGenerateRequest, component);
                                }
                                schema.getSpec().getComponents().add(component);
                            }
                        }
                        break;
                    }
                    case DeployConfigTypeId.TYPE_TRAITS: {
                        String componentType = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_TYPE);
                        String componentName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_NAME);
                        String traitName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_TRAIT);
                        if (StringUtils.isAnyEmpty(componentType, traitName)) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    String.format("invalid trait typeId found, ComponentType/Trait are required|" +
                                            "typeId=%s", typeId));
                        }
                        DeployAppSchema.SpecComponentTrait trait = SchemaUtil
                                .toSchema(DeployAppSchema.SpecComponentTrait.class, config);
                        trait.setName(traitName);
                        if (StringUtils.isEmpty(trait.getRuntime())) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    "the field 'runtime' is required in trait deploy config");
                        }
                        boolean componentFound = false;
                        for (DeployAppSchema.SpecComponent specComponent : schema.getSpec().getComponents()) {
                            DeployAppRevisionName revisionName = DeployAppRevisionName
                                    .valueOf(specComponent.getRevisionName());

                            if (ComponentTypeEnum.isSystemComponentType(componentType)) {
                                // 系统内置类型组件，只需要对自身进行配置加载
                                if (StringUtils.isEmpty(componentName)) {
                                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                            String.format("componentName is required in current component type %s",
                                                    componentType));
                                }
                                if (componentType.equalsIgnoreCase(revisionName.getComponentType())
                                        && componentName.equals(revisionName.getComponentName())) {
                                    componentFound = true;
                                    for (DeployAppSchema.SpecComponentTrait specComponentTrait
                                            : specComponent.getTraits()) {
                                        if (traitName.equals(specComponentTrait.getName())) {
                                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                                    String.format("conflict trait deploy config, which already " +
                                                            "exists at the component level|typeId=%s", typeId));
                                        }
                                    }
                                    specComponent.getTraits().add(trait);
                                }
                            } else {
                                // 用户定义类型组件，需要对当前组件的所有实例进行扫描，并扩展到所有对象
                                if (StringUtils.isNotEmpty(componentName)) {
                                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                            String.format("componentName is not allowed in current component type %s",
                                                    componentType));
                                }
                                if (componentType.equalsIgnoreCase(revisionName.getComponentType())) {
                                    componentFound = true;
                                    for (DeployAppSchema.SpecComponentTrait specComponentTrait
                                            : specComponent.getTraits()) {
                                        if (traitName.equals(specComponentTrait.getName())) {
                                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                                    String.format("conflict trait deploy config, which already " +
                                                            "exists at the component level|typeId=%s", typeId));
                                        }
                                    }
                                    specComponent.getTraits().add(trait);
                                }
                            }
                        }
                        if (!componentFound) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    String.format("cannot find related component when processing trait " +
                                            "deploy config|typeId=%s", typeId));
                        }
                        break;
                    }
                    case DeployConfigTypeId.TYPE_POLICIES: {
                        if (StringUtils.isEmpty(config)) {
                            schema.getSpec().setPolicies(new ArrayList<>());
                        } else {
                            schema.getSpec().setPolicies(SchemaUtil.toSchemaList(DeployAppSchema.Policy.class, config));
                        }
                        break;
                    }
                    case DeployConfigTypeId.TYPE_WORKFLOW: {
                        if (StringUtils.isEmpty(config)) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    "empty workflow config is now allowed");
                        } else {
                            schema.getSpec().setWorkflow(SchemaUtil.toSchema(DeployAppSchema.Workflow.class, config));
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
        return schema;
    }

    /**
     * 检查给定的 Deploy Config 记录是否满足不冲突的前提条件
     *
     * @param records Deploy Config 记录
     * @return true or false (valid or not)
     */
    private String getConflictGlobalTemplateTypeId(List<DeployConfigDO> records) {
        Set<String> usedSet = new HashSet<>();
        for (DeployConfigDO record : records) {
            if (usedSet.contains(record.getTypeId())) {
                return record.getTypeId();
            }
            usedSet.add(record.getTypeId());
        }
        return null;
    }

    /**
     * 生成一个新的 Application Configuration 模板
     *
     * @param req 生成配置请求
     * @return 空的 DeployAppSchema
     */
    private DeployAppSchema emptyDeployAppSchema(DeployConfigGenerateReq req) {
        DeployAppSchema schema = new DeployAppSchema();
        schema.setApiVersion(req.getApiVersion());
        schema.setKind("ApplicationConfiguration");
        schema.setMetadata(DeployAppSchema.MetaData.builder()
                .name(req.getAppId())
                .annotations(DeployAppSchema.MetaDataAnnotations.builder()
                        .unitId(req.getUnitId())
                        .clusterId(req.getClusterId())
                        .namespaceId(req.getNamespaceId())
                        .stageId(req.getStageId())
                        .appId(req.getAppId())
                        .appInstanceName(req.getAppInstanceName())
                        .appPackageId(req.getAppPackageId())
                        .build())
                .build());
        schema.setSpec(new DeployAppSchema.Spec());
        schema.getSpec().setParameterValues(new ArrayList<>());
        schema.getSpec().setComponents(new ArrayList<>());
        return schema;
    }

    /**
     * 根据 deploy config 配置生成 application configuration
     *
     * @param req DeployConfigGenerate 请求
     * @return Application Configuration Schema
     */
    private DeployAppSchema generateByConfig(DeployConfigGenerateReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        String unitId = req.getUnitId();
        String clusterId = req.getClusterId();
        String namespaceId = req.getNamespaceId();
        String stageId = req.getStageId();
        String isolateNamespaceId = req.getIsolateNamespaceId();
        String isolateStageId = req.getIsolateStageId();
        // 没有传入 clusterId 的情况下视为多集群
        boolean multipleCluster = StringUtils.isEmpty(clusterId);

        // 组装每个 type 到 schema 中
        DeployAppSchema schema = emptyDeployAppSchema(req);
        List<DeployConfigDO> appRecords = deployConfigRepository.selectByCondition(
                DeployConfigQueryCondition.builder()
                        .apiVersion(apiVersion)
                        .appId(appId)
                        .enabled(true)
                        .isolateNamespaceId(isolateNamespaceId)
                        .isolateStageId(isolateStageId)
                        .build());
        List<DeployConfigDO> rootRecords = deployConfigRepository.selectByCondition(
                DeployConfigQueryCondition.builder()
                        .apiVersion(apiVersion)
                        .appId("")
                        .enabled(true)
                        .isolateNamespaceId(isolateNamespaceId)
                        .isolateStageId(isolateStageId)
                        .build());
        // 明确是具体特定组件的 typeIds，明确有 componentName 的 (非通用类型 typeIds)
        // 如果包含 envBinding 字段，那么进行 typeId 自动填充 (目前存在两个优先级)
        List<String> typeIds = CollectionUtils.isEmpty(req.getTypeIds())
                ? distinctTypeIds(appRecords)
                : req.getTypeIds();
        List<String> staticEnvPriorities = Arrays.asList(
                EnvUtil.generate(unitId, clusterId, namespaceId, stageId),
                EnvUtil.generate(unitId, clusterId, namespaceId, ""),
                EnvUtil.generate(unitId, "", namespaceId, stageId),
                EnvUtil.generate(unitId, "", namespaceId, ""),
                EnvUtil.generate("", "", namespaceId, stageId),
                EnvUtil.generate("", "", namespaceId, "")
        );
        staticEnvPriorities = staticEnvPriorities.stream().distinct().collect(Collectors.toList());
        String envBindingTypeId = (new DeployConfigTypeId(DeployConfigTypeId.TYPE_ENV_BINDING)).toString();
        Set<String> usedTypeIdSet = appRecords.stream().map(DeployConfigDO::getTypeId).collect(Collectors.toSet());
        for (String staticEnvId : staticEnvPriorities) {
            // 不存在的话，直接 continue 到下个 envId 优先级继续搜索
            List<DeployConfigDO> envBindingRecords = deployConfigRepository.selectByCondition(
                    DeployConfigQueryCondition.builder()
                            .apiVersion(apiVersion)
                            .appId(appId)
                            .enabled(true)
                            .typeId(envBindingTypeId)
                            .envId(staticEnvId)
                            .isolateNamespaceId(isolateNamespaceId)
                            .isolateStageId(isolateStageId)
                            .build());
            if (envBindingRecords.size() == 0) {
                log.info("no Type:envBinding matching records found in deploy config, skip|appId={}|envId={}|" +
                                "isolateNamespaceId={}|isolateStageId={}", appId, staticEnvId, isolateNamespaceId,
                        isolateStageId);
                continue;
            }

            log.info("find Type:envBinding matching records in deploy config|appId={}|envId={}|" +
                    "isolateNamespaceId={}|isolateStageId={}", appId, staticEnvId, isolateNamespaceId, isolateStageId);
            DeployConfigDO mockSource = DeployConfigDO.builder()
                    .apiVersion(req.getApiVersion())
                    .appId(req.getAppId())
                    .typeId("MOCK")
                    .envId(staticEnvId)
                    .currentRevision(0)
                    .enabled(true)
                    .config("")
                    .inherit(false)
                    .productId(envBindingRecords.get(0).getProductId())
                    .releaseId(envBindingRecords.get(0).getReleaseId())
                    .namespaceId(isolateNamespaceId)
                    .stageId(isolateStageId)
                    .build();
            for (AppComponentLocationContainer item : req.getAppComponents()) {
                String typeId = (new DeployConfigTypeId(item.getComponentType(), item.getComponentName())).toString();
                if (!usedTypeIdSet.contains(typeId)) {
                    typeIds.add(typeId);
                    DeployConfigDO record = SerializationUtils.clone(mockSource);
                    record.setTypeId(typeId);
                    appRecords.add(record);
                }
            }

            // 公共类型直接加入
            for (String typeId : Arrays.asList(
                    (new DeployConfigTypeId(DeployConfigTypeId.TYPE_PARAMETER_VALUES)).toString(),
                    (new DeployConfigTypeId(DeployConfigTypeId.TYPE_POLICIES)).toString(),
                    (new DeployConfigTypeId(DeployConfigTypeId.TYPE_WORKFLOW)).toString())) {
                typeIds.add(typeId);
                DeployConfigDO record = SerializationUtils.clone(mockSource);
                record.setTypeId(typeId);
                appRecords.add(record);
            }

            break;
        }
        // 最后删除 envBinding 自身
        typeIds.remove(envBindingTypeId);
        typeIds = typeIds.stream().distinct().collect(Collectors.toList());

        // step==0 非 traits 类型组装, step==1 traits 类型组装 (step==1 依赖 step==0 完成)
        log.info("after searching, there are some app records in deploy config|appId={}|isolateNamespaceId={}|" +
                        "isolateStageId={}|typeIds={}|appRecords={}", appId, isolateNamespaceId, isolateStageId,
                JSONArray.toJSONString(typeIds), JSONArray.toJSONString(appRecords));
        for (int step = 0; step < 2; step++) {
            for (String typeId : typeIds) {
                DeployConfigTypeId typeIdObj = DeployConfigTypeId.valueOf(typeId);
                if (step == 0 && DeployConfigTypeId.TYPE_TRAITS.equals(typeIdObj.getType())) {
                    continue;
                } else if (step == 1 && !DeployConfigTypeId.TYPE_TRAITS.equals(typeIdObj.getType())) {
                    continue;
                }

                List<DeployConfigDO> filterAppRecords = appRecords.stream()
                        .filter(item -> item.getTypeId().equals(typeId))
                        .collect(Collectors.toList());
                if (filterAppRecords.size() == 0) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("cannot find the suitable app records when combines application|" +
                                            "appId=%s|unitId=%s|clusterId=%s|namespaceId=%s|stageId=%s|typeId=%s",
                                    appId, unitId, clusterId, namespaceId, stageId, typeId));
                }
                List<DeployConfigDO> filterRootRecords = rootRecords.stream()
                        .filter(item -> item.getTypeId().equals(typeId))
                        .collect(Collectors.toList());
                List<DeployConfigDO> filterRootParentRecords = rootRecords.stream()
                        .filter(item -> item.getTypeId().equals(typeIdObj.parentTypeId()))
                        .collect(Collectors.toList());
                DeployConfigDO best = findBestConfigInRecordsBySpecifiedName(
                        filterAppRecords, filterRootRecords, filterRootParentRecords,
                        unitId, clusterId, namespaceId, stageId);
                if (best == null) {
                    log.info("no best deploy config found by given condition|appId={}|unitId={}|clusterId={}|" +
                                    "namespaceId={}|stageId={}|typeId={}, skip", appId, unitId, clusterId,
                            namespaceId, stageId, typeId);
                    continue;
                }
                log.info("find the best deploy config by given condition|appId={}|unitId={}|clusterId={}|" +
                                "namespaceId={}|stageId={}|typeId={}|best={}", appId, unitId, clusterId,
                        namespaceId, stageId, typeId, JSONObject.toJSONString(best));
                String config = best.getConfig();
                if (StringUtils.isNotEmpty(best.getProductId()) && StringUtils.isNotEmpty(best.getReleaseId())) {
                    config = fetchConfigInGit(best.getProductId(), best.getReleaseId(), appId, typeId);
                    if (StringUtils.isEmpty(config)) {
                        log.info("cannot fetch config in git by given condition, skip|productId={}|releaseId={}|" +
                                "appId={}|typeId={}", best.getProductId(), best.getReleaseId(), appId, typeId);
                        continue;
                    } else {
                        log.info("fetch config in git succeed|productId={}|releaseId={}|appId={}|typeId={}|config={}",
                                best.getProductId(), best.getReleaseId(), appId, typeId, config);
                    }
                } else {
                    if (StringUtils.isEmpty(config)) {
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                String.format("invalid inherit config, cannot get config by typeId %s|appRecords=%s|" +
                                                "rootRecords=%s|rootParentRecords=%s", typeId,
                                        JSONArray.toJSONString(filterAppRecords),
                                        JSONArray.toJSONString(filterRootRecords),
                                        JSONArray.toJSONString(filterRootParentRecords)));
                    }
                }

                switch (typeIdObj.getType()) {
                    case DeployConfigTypeId.TYPE_PARAMETER_VALUES: {
                        schema.getSpec().setParameterValues(
                                SchemaUtil.toSchemaList(DeployAppSchema.ParameterValue.class, config));
                        break;
                    }
                    case DeployConfigTypeId.TYPE_COMPONENTS: {
                        String componentType = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_TYPE);
                        String componentName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_NAME);
                        DeployAppSchema.SpecComponent component = SchemaUtil
                                .toSchema(DeployAppSchema.SpecComponent.class, config);
                        // 如果是单集群目标部署模式，或者是系统组件，那么默认填充 scopes 参数
                        if (!multipleCluster || ComponentTypeEnum.isSystemComponentType(componentType)) {
                            component = enrichComponentScopes(req, component);
                        }
                        // 如果当前的 componentName 没有在 specComponent revisionName 下，说明是通用配置
                        // 那么需要使用当前 typeId 中的值进行填充
                        if (StringUtils.isEmpty(DeployAppRevisionName
                                .valueOf(component.getRevisionName()).getComponentName())) {
                            component.setRevisionName(
                                    (new DeployAppRevisionName(componentType, componentName, "_")).revisionName());
                        }
                        schema.getSpec().getComponents().add(component);
                        break;
                    }
                    case DeployConfigTypeId.TYPE_TRAITS: {
                        String componentType = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_TYPE);
                        String componentName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_COMPONENT_NAME);
                        String traitName = typeIdObj.getAttr(DeployConfigTypeId.ATTR_TRAIT);
                        if (StringUtils.isAnyEmpty(componentType, componentName, traitName)) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    String.format("invalid trait typeId found, ComponentType/ComponentName/Trait " +
                                            "are required|typeId=%s", typeId));
                        }
                        DeployAppSchema.SpecComponentTrait trait = SchemaUtil
                                .toSchema(DeployAppSchema.SpecComponentTrait.class, config);
                        trait.setName(traitName);
                        if (StringUtils.isEmpty(trait.getRuntime())) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    "the field 'runtime' is required in trait deploy config");
                        }
                        boolean componentFound = false;
                        for (DeployAppSchema.SpecComponent specComponent : schema.getSpec().getComponents()) {
                            DeployAppRevisionName revisionName = DeployAppRevisionName
                                    .valueOf(specComponent.getRevisionName());
                            if (componentType.equalsIgnoreCase(revisionName.getComponentType())
                                    && componentName.equals(revisionName.getComponentName())) {
                                componentFound = true;
                                for (DeployAppSchema.SpecComponentTrait specComponentTrait
                                        : specComponent.getTraits()) {
                                    if (traitName.equals(specComponentTrait.getName())) {
                                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                                String.format("conflict trait deploy config, which already " +
                                                        "exists at the component level|typeId=%s", typeId));
                                    }
                                }
                                specComponent.getTraits().add(trait);
                            }
                        }
                        if (!componentFound) {
                            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                    String.format("cannot find related component when processing trait " +
                                            "deploy config|typeId=%s", typeId));
                        }
                        break;
                    }
                    case DeployConfigTypeId.TYPE_POLICIES: {
                        schema.getSpec().setPolicies(SchemaUtil.toSchemaList(DeployAppSchema.Policy.class, config));
                        break;
                    }
                    case DeployConfigTypeId.TYPE_WORKFLOW: {
                        schema.getSpec().setWorkflow(SchemaUtil.toSchema(DeployAppSchema.Workflow.class, config));
                        break;
                    }
                    default:
                        break;
                }
            }
        }

        // 检查是否有 components 被完成组装，如果没有的话，说明本次生成 Yaml 请求无效
        if (schema.getSpec().getComponents().size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("the current application does not have permission to access the environment|" +
                                    "appId=%s|unitId=%s|clusterId=%s|namespaceId=%s|stageId=%s|isolateNamespaceId=%s|" +
                                    "isolateStageId=%s",
                            appId, unitId, clusterId, namespaceId, stageId, isolateNamespaceId, isolateStageId));
        }
        return schema;
    }

    /**
     * 针对目标 Scope 进行 Cluster/Namespace/Stage 覆盖
     *
     * @param req    请求
     * @param schema ApplicationConfiguration 中的 SpecComponent Schema
     * @return DeployAppSchema.SpecComponent
     */
    @Override
    public DeployAppSchema.SpecComponent enrichComponentScopes(
            DeployConfigGenerateReq req, DeployAppSchema.SpecComponent schema) {
        boolean clusterFlag = false;
        boolean namespaceFlag = false;
        boolean stageFlag = false;
        for (DeployAppSchema.SpecComponentScope scope : schema.getScopes()) {
            DeployAppSchema.SpecComponentScopeRef ref = scope.getScopeRef();
            switch (ref.getKind()) {
                case "Cluster":
                    if (StringUtils.isNotEmpty(req.getClusterId())) {
                        ref.setName(req.getClusterId());
                    } else {
                        ref.setName("");
                    }
                    clusterFlag = true;
                    break;
                case "Namespace":
                    if (StringUtils.isNotEmpty(req.getNamespaceId())) {
                        ref.setName(req.getNamespaceId());
                    } else {
                        ref.setName("");
                    }
                    namespaceFlag = true;
                    break;
                case "Stage":
                    if (StringUtils.isNotEmpty(req.getStageId())) {
                        ref.setName(req.getStageId());
                    } else {
                        ref.setName("");
                    }
                    stageFlag = true;
                    break;
                default:
                    break;
            }
        }
        if (!clusterFlag) {
            schema.getScopes().add(DeployAppSchema.SpecComponentScope.builder()
                    .scopeRef(DeployAppSchema.SpecComponentScopeRef.builder()
                            .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                            .kind("Cluster")
                            .name(StringUtils.isEmpty(req.getClusterId()) ? "" : req.getClusterId())
                            .spec(new JSONObject())
                            .build())
                    .build());
        }
        if (!namespaceFlag) {
            schema.getScopes().add(DeployAppSchema.SpecComponentScope.builder()
                    .scopeRef(DeployAppSchema.SpecComponentScopeRef.builder()
                            .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                            .kind("Namespace")
                            .name(StringUtils.isEmpty(req.getNamespaceId()) ? "" : req.getNamespaceId())
                            .spec(new JSONObject())
                            .build())
                    .build());
        }
        if (!stageFlag) {
            schema.getScopes().add(DeployAppSchema.SpecComponentScope.builder()
                    .scopeRef(DeployAppSchema.SpecComponentScopeRef.builder()
                            .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                            .kind("Stage")
                            .name(StringUtils.isEmpty(req.getStageId()) ? "" : req.getStageId())
                            .spec(new JSONObject())
                            .build())
                    .build());
        }
        return schema;
    }

    /**
     * 过滤 deploy config 中指定 envId 的数据，并按照优先级排序
     *
     * @param records deploy config 列表
     * @param envId   环境 ID
     * @return 过滤及排序后的数据
     */
    private List<DeployConfigDO> filterDeployConfigByEnvId(List<DeployConfigDO> records, String envId) {
        return records.stream()
                .filter(item -> item.getEnvId().contains(envId))
                .sorted((o1, o2) -> {
                    int o1Length = o1.getEnvId().split("::").length;
                    int o2Length = o2.getEnvId().split("::").length;
                    return Integer.compare(o2Length, o1Length);
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据指定条件寻找最佳部署配置
     *
     * @param records     根 deploy config 配置 (无 appId，全局配置)
     * @param clusterId   集群 ID
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     * @return 最佳配置记录
     */
    @Override
    public DeployConfigDO findBestConfigInRecordsByGeneralType(
            List<DeployConfigDO> records, String clusterId, String namespaceId, String stageId) {
        List<String> priorities = new ArrayList<>();
        if (StringUtils.isNotEmpty(stageId)) {
            priorities.add(DeployConfigEnvId.stageStr(stageId));
        }
        if (StringUtils.isNotEmpty(namespaceId)) {
            priorities.add(DeployConfigEnvId.namespaceStr(namespaceId));
        }
        if (StringUtils.isNotEmpty(clusterId)) {
            priorities.add(DeployConfigEnvId.clusterStr(clusterId));
        }
        for (String current : priorities) {
            List<DeployConfigDO> filteredRecords = filterDeployConfigByEnvId(records, current);
            if (filteredRecords.size() > 0) {
                return filteredRecords.get(0);
            }
        }
        throw new AppException(AppErrorCode.DEPLOY_ERROR,
                String.format("cannot find best deploy config with given condition(general type)|" +
                        "clusterId=%s|namespaceId=%s|stageId=%s", clusterId, namespaceId, stageId));
    }

    /**
     * 根据指定条件寻找最佳部署配置
     *
     * @param appRecords        指定应用下的 deploy config 配置
     * @param rootRecords       根 deploy config 配置 (无 appId, 全局配置)
     * @param rootParentRecords rootRecords 的 parent type id 对应的过滤 deploy config 配置 (无 appId, 全局配置)
     * @param unitId            单元 ID
     * @param clusterId         集群 ID
     * @param namespaceId       Namespace ID
     * @param stageId           Stage ID
     * @return 最佳配置记录 (如果返回 null，表明当前无异常，但无法由给定条件找到合理数据，应当由上层忽略此次查找)
     */
    @Override
    public DeployConfigDO findBestConfigInRecordsBySpecifiedName(
            List<DeployConfigDO> appRecords, List<DeployConfigDO> rootRecords, List<DeployConfigDO> rootParentRecords,
            String unitId, String clusterId, String namespaceId, String stageId) {
        List<String> priorities = new ArrayList<>();
        if (StringUtils.isNotEmpty(unitId) && StringUtils.isNotEmpty(namespaceId) && StringUtils.isNotEmpty(stageId)) {
            priorities.add(DeployConfigEnvId.unitNamespaceStageStr(unitId, namespaceId, stageId));
        }
        if (StringUtils.isNotEmpty(unitId) && StringUtils.isNotEmpty(namespaceId)) {
            priorities.add(DeployConfigEnvId.unitNamespaceStr(unitId, namespaceId));
        }
        if (StringUtils.isNotEmpty(stageId)) {
            priorities.add(DeployConfigEnvId.stageStr(stageId));
        }
        if (StringUtils.isNotEmpty(namespaceId)) {
            priorities.add(DeployConfigEnvId.namespaceStr(namespaceId));
        }
        if (StringUtils.isNotEmpty(clusterId)) {
            priorities.add(DeployConfigEnvId.clusterStr(clusterId));
        }
        if (StringUtils.isNotEmpty(unitId)) {
            priorities.add(DeployConfigEnvId.unitStr(unitId));
        }
        for (String current : priorities) {
            List<DeployConfigDO> filteredAppRecords = filterDeployConfigByEnvId(appRecords, current);
            if (filteredAppRecords.size() == 0) {
                continue;
            }

            DeployConfigDO result = filteredAppRecords.get(0);
            if (result.getInherit() != null && result.getInherit()) {
                List<DeployConfigDO> filteredRootRecords = filterDeployConfigByEnvId(rootRecords, current);
                if (filteredRootRecords.size() > 0) {
                    return filteredRootRecords.get(0);
                } else {
                    // 第一层找不到后，向上提一层 TypeId 进行继承查找，如果仍然找不到，返回 null
                    filteredRootRecords = filterDeployConfigByEnvId(rootParentRecords, current);
                    if (filteredRootRecords.size() > 0) {
                        return filteredRootRecords.get(0);
                    }
                }
                return null;
            } else {
                return result;
            }
        }

        // 通过无 envId 的部署配置记录进行二次查找
        // 该记录查找项属于应用层面的公共基线配置，及向上从 envId 层面环境配置进行公共配置继承
        List<DeployConfigDO> filteredAppRecords = appRecords.stream()
                .filter(item -> StringUtils.isEmpty(item.getEnvId()))
                .collect(Collectors.toList());
        if (filteredAppRecords.size() == 0) {
            throw new AppException(AppErrorCode.DEPLOY_ERROR,
                    String.format("cannot find best deploy config with given condition(specified name)|unitId=%s|" +
                                    "clusterId=%s|namespaceId=%s|stageId=%s|appRecords=%s|rootRecords=%s|" +
                                    "rootParentRecords=%s", unitId, clusterId, namespaceId, stageId,
                            JSONArray.toJSONString(appRecords), JSONArray.toJSONString(rootRecords),
                            JSONArray.toJSONString(rootParentRecords)));
        }

        DeployConfigDO result = filteredAppRecords.get(0);
        if (result.getInherit() != null && result.getInherit()) {
            for (String current : priorities) {
                List<DeployConfigDO> filteredRootRecords = filterDeployConfigByEnvId(rootRecords, current);
                if (filteredRootRecords.size() > 0) {
                    return filteredRootRecords.get(0);
                }
            }
            List<DeployConfigDO> filterRootRecords = rootRecords.stream()
                    .filter(item -> StringUtils.isEmpty(item.getEnvId()))
                    .collect(Collectors.toList());
            if (filterRootRecords.size() > 0) {
                return filterRootRecords.get(0);
            }
            return null;
        } else {
            return result;
        }
    }

    /**
     * 获取 Deploy Config 列表中 type id 的 distinct 列表
     *
     * @param deployConfigs Deploy Config 配置对象
     * @return distinct type ids
     */
    private List<String> distinctTypeIds(List<DeployConfigDO> deployConfigs) {
        Set<String> result = new HashSet<>();
        for (DeployConfigDO config : deployConfigs) {
            result.add(config.getTypeId());
        }
        return new ArrayList<>(result);
    }

    /**
     * 删除单个部署模板配置
     *
     * @param apiVersion       API Version
     * @param appId            应用 ID
     * @param typeId           类型 ID
     * @param envId            环境 ID
     * @param isolateNamespace Namespace ID
     * @param isolateStage     Stage ID
     */
    private void deleteSingleConfig(
            String apiVersion, String appId, String typeId, String envId, String isolateNamespace, String isolateStage) {
        if (StringUtils.isEmpty(appId)) {
            appId = "";
        }
        if (StringUtils.isEmpty(envId)) {
            envId = "";
        }

        // 获取当前配置
        DeployConfigQueryCondition condition = DeployConfigQueryCondition.builder()
                .appId(appId)
                .typeId(typeId)
                .envId(envId)
                .apiVersion(apiVersion)
                .isolateNamespaceId(isolateNamespace)
                .isolateStageId(isolateStage)
                .page(1)
                .pageSize(1)
                .build();
        List<DeployConfigDO> records = deployConfigRepository.selectByCondition(condition);
        if (records.size() == 0) {
            log.info("no need to delete single deploy config record|apiVersion={}|appId={}|typeId={}|envId={}|" +
                            "namespaceId={}|stageId={}", apiVersion, appId, typeId, envId,
                    isolateNamespace, isolateStage);
            return;
        } else if (records.size() > 1) {
            String errorMessage = String.format("system error, multiple deploy config records found|apiVersion=%s|" +
                            "appId=%s|typeId=%s|envId=%s|namespaceId=%s|stageId=%s", apiVersion, appId, typeId, envId,
                    isolateNamespace, isolateStage);
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage);
        }
        Integer revision = records.get(0).getCurrentRevision() + 1;
        String config = records.get(0).getConfig();

        // 保存数据
        deployConfigHistoryRepository.insertSelective(DeployConfigHistoryDO.builder()
                .appId(appId)
                .typeId(typeId)
                .envId(envId)
                .apiVersion(apiVersion)
                .revision(revision)
                .config(config)
                .inherit(false)
                .deleted(true)
                .namespaceId(isolateNamespace)
                .stageId(isolateStage)
                .build());
        deployConfigRepository.deleteByCondition(condition);
        log.info("deploy config record has deleted|apiVersion={}|appId={}|typeId={}|envId={}|namespaceId={}|stageId={}",
                apiVersion, appId, typeId, envId, isolateNamespace, isolateStage);
    }

    /**
     * 保存单个部署模板到系统中并应用
     *
     * @param apiVersion         API Version
     * @param appId              应用 ID
     * @param typeId             类型 ID
     * @param envId              环境 ID
     * @param config             配置内容
     * @param enabled            是否启用
     * @param inherit            是否继承
     * @param isolateNamespaceId 隔离 Namespace ID
     * @param isolateStageId     隔离 Stage ID
     * @param productId          产品 ID
     * @param releaseId          发布版本 ID
     * @return DeployConfigDO
     */
    private DeployConfigDO applySingleConfig(
            String apiVersion, String appId, String typeId, String envId, String config,
            boolean enabled, boolean inherit, String isolateNamespaceId, String isolateStageId,
            String productId, String releaseId) {
        if (StringUtils.isEmpty(envId)) {
            envId = "";
        }

        // 获取当前条件下的下一个版本号
        List<DeployConfigHistoryDO> histories = deployConfigHistoryRepository.selectByExample(
                DeployConfigHistoryQueryCondition.builder()
                        .appId(appId)
                        .typeId(typeId)
                        .envId(envId)
                        .apiVersion(apiVersion)
                        .isolateNamespaceId(isolateNamespaceId)
                        .isolateStageId(isolateStageId)
                        .page(1)
                        .pageSize(1)
                        .build());
        int revision = 0;
        if (CollectionUtils.isNotEmpty(histories)) {
            revision = histories.get(0).getRevision() + 1;
        }

        // 保存数据
        deployConfigHistoryRepository.insertSelective(DeployConfigHistoryDO.builder()
                .appId(appId)
                .typeId(typeId)
                .envId(envId)
                .apiVersion(apiVersion)
                .revision(revision)
                .config(config)
                .inherit(inherit)
                .deleted(false)
                .namespaceId(isolateNamespaceId)
                .stageId(isolateStageId)
                .productId(productId)
                .releaseId(releaseId)
                .build());
        DeployConfigQueryCondition configCondition = DeployConfigQueryCondition.builder()
                .appId(appId)
                .typeId(typeId)
                .envId(envId)
                .apiVersion(apiVersion)
                .isolateNamespaceId(isolateNamespaceId)
                .isolateStageId(isolateStageId)
                .build();
        List<DeployConfigDO> records = deployConfigRepository.selectByCondition(configCondition);
        DeployConfigDO result;
        if (records.size() == 0) {
            result = DeployConfigDO.builder()
                    .appId(appId)
                    .typeId(typeId)
                    .envId(envId)
                    .apiVersion(apiVersion)
                    .currentRevision(revision)
                    .config(config)
                    .enabled(enabled)
                    .inherit(inherit)
                    .namespaceId(isolateNamespaceId)
                    .stageId(isolateStageId)
                    .productId(productId)
                    .releaseId(releaseId)
                    .build();
            try {
                deployConfigRepository.insert(result);
            } catch (Exception e) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot insert deploy config into database|result=%s|exception=%s",
                                JSONObject.toJSONString(result), ExceptionUtils.getStackTrace(e)));
            }
            log.info("deploy config has insert into database|apiVersion={}|appId={}|typeId={}|envId={}|revision={}|" +
                            "enable={}|inherit={}|namespaceId={}|stageId={}", apiVersion, appId, typeId, envId, revision,
                    enabled, inherit, isolateNamespaceId, isolateStageId);
        } else {
            DeployConfigDO item = records.get(0);
            item.setCurrentRevision(revision);
            item.setConfig(config);
            item.setEnabled(enabled);
            item.setInherit(inherit);
            item.setProductId(productId);
            item.setReleaseId(releaseId);
            try {
                deployConfigRepository.updateByCondition(item, configCondition);
            } catch (Exception e) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot update deploy config in database|item=%s|condition=%s|exception=%s",
                                JSONObject.toJSONString(item), JSONObject.toJSONString(configCondition),
                                ExceptionUtils.getStackTrace(e)));
            }
            log.info("deploy config has updated in database|apiVersion={}|appId={}|typeId={}|envId={}|revision={}|" +
                            "enable={}|inherit={}|namespaceId={}|stageId={}|productId={}|releaseId={}", apiVersion,
                    appId, typeId, envId, revision, enabled, inherit, isolateNamespaceId, isolateStageId, productId,
                    releaseId);
            result = item;
        }
        return result;
    }

    /**
     * 根据产品/发布版本对应的基线配置，从 Git 中获取对应的 Application Configuration 片段信息
     *
     * @param productId 产品 ID
     * @param releaseId 发布版本 ID
     * @param appId     应用 ID
     * @param typeId    类型 ID
     * @return Config 内容 (如果对应 component 组件不存在，则返回 null; 其他公共类型找不到直接抛异常)
     */
    private String fetchConfigInGit(String productId, String releaseId, String appId, String typeId) {
        ProductReleaseProvider productReleaseProvider = BeanUtil.getBean(ProductReleaseProvider.class);
        String config;
        try {
            config = productReleaseProvider.getLaunchYaml(productId, releaseId, appId);
        } catch (AppException e) {
            if (AppErrorCode.INVALID_USER_ARGS.equals(e.getErrorCode())) {
                return null;
            }
            throw e;
        }
        DeployAppSchema schema = SchemaUtil.toSchema(DeployAppSchema.class, config);
        DeployConfigTypeId deployConfigType = DeployConfigTypeId.valueOf(typeId);
        String errorMessage = String.format("could not find a component configuration that satisfies the condition|" +
                        "productId=%s|releaseId=%s|appId=%s|typeId=%s",
                productId, releaseId, appId, typeId);
        switch (deployConfigType.getType()) {
            case DeployConfigTypeId.TYPE_PARAMETER_VALUES:
                return SchemaUtil.toYamlStr(schema.getSpec().getParameterValues(),
                        DeployAppSchema.ParameterValue.class);
            case DeployConfigTypeId.TYPE_COMPONENTS:
                String componentType = deployConfigType.getAttr(DeployConfigTypeId.ATTR_COMPONENT_TYPE);
                String componentName = deployConfigType.getAttr(DeployConfigTypeId.ATTR_COMPONENT_NAME);
                if (componentType == null || componentName == null) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage);
                }
                String revisionName = String.format("%s|%s|_", componentType, componentName);
                for (DeployAppSchema.SpecComponent component : schema.getSpec().getComponents()) {
                    if (component.getRevisionName().equals(revisionName)) {
                        return SchemaUtil.toYamlMapStr(component);
                    }
                }
                return null;
            case DeployConfigTypeId.TYPE_POLICIES:
                return SchemaUtil.toYamlStr(schema.getSpec().getPolicies(), DeployAppSchema.Policy.class);
            case DeployConfigTypeId.TYPE_WORKFLOW:
                return SchemaUtil.toYamlMapStr(schema.getSpec().getWorkflow());
            default:
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage);
        }
    }
}
