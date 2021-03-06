package com.alibaba.tesla.appmanager.server.service.apppackage.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties;
import com.alibaba.tesla.appmanager.common.constants.CheckNullObject;
import com.alibaba.tesla.appmanager.common.constants.CustomComponentConstant;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.AddonTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployAppAttrTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.DeployAppStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ObjectUtil;
import com.alibaba.tesla.appmanager.common.util.PackageUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.VersionUtil;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.core.CustomWorkloadResource;
import com.alibaba.tesla.appmanager.domain.core.StorageFile;
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageVersionCountDTO;
import com.alibaba.tesla.appmanager.domain.dto.ParamBinderDTO;
import com.alibaba.tesla.appmanager.domain.dto.TraitBinderDTO;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageCreateByStreamReq;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageReleaseReq;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageVersionCountReq;
import com.alibaba.tesla.appmanager.domain.req.apppackage.ApplicationConfigurationGenerateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigGenerateReq;
import com.alibaba.tesla.appmanager.domain.res.apppackage.ApplicationConfigurationGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema.MetaData;
import com.alibaba.tesla.appmanager.domain.schema.CustomAddonSchema;
import com.alibaba.tesla.appmanager.domain.schema.CustomAddonSchema.Spec;
import com.alibaba.tesla.appmanager.domain.schema.CustomAddonWorkloadSpec;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.server.repository.AppPackageComponentRelRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageTagRepository;
import com.alibaba.tesla.appmanager.server.repository.CustomAddonMetaRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.*;
import com.alibaba.tesla.appmanager.server.repository.domain.*;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageService;
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageService;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployAppService;
import com.alibaba.tesla.appmanager.server.service.deploy.business.DeployAppBO;
import com.alibaba.tesla.appmanager.server.storage.Storage;
import com.google.common.base.Enums;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ?????? - ???????????????
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppPackageServiceImpl implements AppPackageService {

    private final AppPackageRepository appPackageRepository;
    private final AppPackageTagRepository appPackageTagRepository;
    private final AppPackageComponentRelRepository relRepository;
    private final ComponentPackageService componentPackageService;
    private final CustomAddonMetaRepository customAddonMetaRepository;
    private final DeployAppService deployAppService;
    private final PackageProperties packageProperties;
    private final Storage storage;
    private final DeployConfigService deployConfigService;

    public AppPackageServiceImpl(
            AppPackageRepository appPackageRepository, AppPackageTagRepository appPackageTagRepository,
            AppPackageComponentRelRepository relRepository, ComponentPackageService componentPackageService,
            CustomAddonMetaRepository customAddonMetaRepository, DeployAppService deployAppService,
            PackageProperties packageProperties, Storage storage, DeployConfigService deployConfigService) {
        this.appPackageRepository = appPackageRepository;
        this.appPackageTagRepository = appPackageTagRepository;
        this.relRepository = relRepository;
        this.componentPackageService = componentPackageService;
        this.customAddonMetaRepository = customAddonMetaRepository;
        this.deployAppService = deployAppService;
        this.packageProperties = packageProperties;
        this.storage = storage;
        this.deployConfigService = deployConfigService;
    }

    /**
     * ?????????????????????????????????
     *
     * @param condition ????????????
     * @return List
     */
    @Override
    public Pagination<AppPackageDO> list(AppPackageQueryCondition condition) {
        List<AppPackageDO> appPackageList = appPackageRepository.selectByCondition(condition);
        List<Long> appPackageIdList = appPackageList.stream()
                .map(AppPackageDO::getId)
                .collect(Collectors.toList());
        List<AppPackageTagDO> tagList = appPackageTagRepository.query(appPackageIdList);
        appPackageList.forEach(appPackage -> appPackage.setTags(tagList.stream()
                .filter(p -> p.getAppPackageId().equals(appPackage.getId())
                        && StringUtils.isNotEmpty(appPackage.getPackagePath()))
                .map(AppPackageTagDO::getTag)
                .collect(Collectors.toList())));
        return Pagination.valueOf(appPackageList, Function.identity());
    }

    /**
     * ?????????????????????????????????
     *
     * @param condition ????????????
     * @return AppPackageDO
     */
    @Override
    public AppPackageDO get(AppPackageQueryCondition condition) {
        AppPackageDO appPackage = appPackageRepository.getByCondition(condition);
        if (appPackage == null) {
            return null;
        }

        List<AppPackageTagDO> tagList = appPackageTagRepository.query(Collections.singletonList(appPackage.getId()));
        appPackage.setTags(tagList.stream().map(AppPackageTagDO::getTag).collect(Collectors.toList()));
        return appPackage;
    }

    /**
     * ???????????? appId ????????????????????? tag ??? package ??????
     *
     * @param req ????????????
     * @return ????????????
     */
    @Override
    public List<AppPackageVersionCountDTO> countVersion(AppPackageVersionCountReq req) {
        return appPackageRepository.countPackageByCondition(AppPackageVersionCountQueryCondition.builder()
                .appIds(req.getAppIds())
                .tag(req.getTag())
                .build());
    }

    /**
     * ?????? Stream ?????????????????????
     *
     * @param req ????????????
     * @return
     */
    @Override
    public AppPackageDO createByStream(AppPackageCreateByStreamReq req) {
        boolean force = req.isForce();
        boolean resetVersion = req.isResetVersion();
        if (force && resetVersion) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "cannot use force and resetVersion parameters at the same time");
        }

        // ?????????????????????????????????????????????????????? item
        String appId = req.getAppId();
        String requestPackageVersion = req.getPackageVersion();
        String actualPackageVersion;
        if (resetVersion) {
            actualPackageVersion = getNextVersion(appId);
        } else if (StringUtils.isNotEmpty(requestPackageVersion)) {
            actualPackageVersion = requestPackageVersion;
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "cannot upload app package with empty packageVersion and resetVersion==false");
        }

        // ????????? storage ???
        String bucketName = packageProperties.getBucketName();
        String objectName = PackageUtil.buildAppPackageRemotePath(appId, actualPackageVersion);
        storage.putObject(bucketName, objectName, req.getBody());
        StorageFile storageFile = new StorageFile();
        storageFile.setBucketName(bucketName);
        storageFile.setObjectName(objectName);
        String actualPackagePath = storageFile.toPath();

        // ?????????????????????
        String packageCreator = req.getPackageCreator();
        AppPackageDO item = AppPackageDO.builder()
                .appId(appId)
                .componentCount(0L)
                .packageVersion(actualPackageVersion)
                .packagePath(actualPackagePath)
                .packageCreator(packageCreator)
                .build();

        // ????????????????????????????????????
        if (resetVersion) {
            appPackageRepository.insert(item);
            return item;
        }

        // ????????????????????????????????????????????????????????????????????????????????????
        AppPackageDO appPackageInDatabase = appPackageRepository.getByCondition(
                AppPackageQueryCondition.builder()
                        .appId(appId)
                        .packageVersion(actualPackageVersion)
                        .withBlobs(true)
                        .build());
        if (appPackageInDatabase == null) {
            appPackageRepository.insert(item);
            return item;
        }
        if (!force) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "cannot update app package, the same record already exists in system");
        }

        // ?????????????????????????????????????????????????????????
        appPackageInDatabase.setPackagePath(actualPackagePath);
        appPackageInDatabase.setPackageCreator(packageCreator);
        appPackageInDatabase.setPackageMd5(null);
        appPackageInDatabase.setAppSchema(null);
        appPackageInDatabase.setSwapp(null);
        appPackageInDatabase.setComponentCount(0L);
        appPackageRepository.updateByPrimaryKeySelective(appPackageInDatabase);
        return appPackageInDatabase;
    }

    /**
     * ???????????????????????????????????? ApplicationConfiguration ??????
     *
     * @param req ??????????????????
     * @return ApplicationConfiguration
     */
    @Override
    public ApplicationConfigurationGenerateRes generate(ApplicationConfigurationGenerateReq req) {
        String apiVersion = req.getApiVersion();
        String appId = req.getAppId();
        long appPackageId = req.getAppPackageId();
        if (StringUtils.isAnyEmpty(apiVersion, appId) || appPackageId == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid generate request|request=%s", JSONObject.toJSONString(req)));
        }

        DeployConfigGenerateRes systemRes = deployConfigService.generate(DeployConfigGenerateReq.builder()
                .apiVersion(req.getApiVersion())
                .appId(req.getAppId())
                .appPackageId(req.getAppPackageId())
                .appInstanceName(req.getAppInstanceName())
                .unitId(req.getUnitId())
                .clusterId(req.getClusterId())
                .namespaceId(req.getNamespaceId())
                .stageId(req.getStageId())
                .typeIds(req.getTypeIds())
                .disableComponentFetching(req.isComponentPackageConfigurationFirst())
                .isolateNamespaceId(req.getIsolateNamespaceId())
                .isolateStageId(req.getIsolateStageId())
                .build());
        DeployAppSchema schema = systemRes.getSchema();

        // merge ????????????????????????????????????????????????
        List<AppPackageComponentRelDO> rels = relRepository.selectByCondition(
                AppPackageComponentRelQueryCondition.builder()
                        .appId(appId)
                        .appPackageId(appPackageId)
                        .build());
        if (rels.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find app package component relations|appId=%s|appPackageId=%d",
                            appId, appPackageId));
        }
        Pagination<ComponentPackageDO> componentPackages = componentPackageService.list(
                ComponentPackageQueryCondition.builder()
                        .idList(rels.stream()
                                .map(AppPackageComponentRelDO::getComponentPackageId)
                                .collect(Collectors.toList()))
                        .withBlobs(true)
                        .pageSize(DefaultConstant.UNLIMITED_PAGE_SIZE)
                        .build());
        mergeApplicationConfiguration(req, schema, componentPackages.getItems());

        // ?????????????????? ID ??????????????? merge ??????
        if (StringUtils.isNotEmpty(req.getUnitId())) {
            schema.getMetadata().getAnnotations().setUnitId(req.getUnitId());
            List<DeployAppSchema.SpecComponent> components = schema.getSpec().getComponents();
            for (DeployAppSchema.SpecComponent component : components) {
                component.getScopes()
                        .add(DeployAppSchema.SpecComponentScope.builder().scopeRef(
                                        DeployAppSchema.SpecComponentScopeRef.builder().kind(DefaultConstant.UNIT).name(req.getUnitId()).build())
                                .build());
            }
        }

        return ApplicationConfigurationGenerateRes.builder()
                .yaml(SchemaUtil.toYamlMapStr(schema))
                .build();
    }

    /**
     * ?????? Component Package ?????? package options ??? application configuration ???
     *
     * @param req               ??????????????????
     * @param schema            Application Configuration Schema
     * @param componentPackages Component Packages
     */
    private void mergeApplicationConfiguration(
            ApplicationConfigurationGenerateReq req, DeployAppSchema schema,
            List<ComponentPackageDO> componentPackages) {
        Set<String> hitRevisionSet = new HashSet<>();
        for (ComponentPackageDO componentPackage : componentPackages) {
            ComponentTypeEnum componentType = Enums.getIfPresent(
                    ComponentTypeEnum.class, componentPackage.getComponentType()).orNull();
            String componentName = componentPackage.getComponentName();
            String packageOptionStr = componentPackage.getPackageOptions();
            if (componentType == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("unsupported component type %s", componentPackage.getComponentType()));
            }
            if (StringUtils.isEmpty(packageOptionStr)) {
                continue;
            }
            JSONObject packageOption = JSONObject.parseObject(packageOptionStr);

            // ???????????? schema ???????????? SpecComponent
            DeployAppSchema.SpecComponent currentComponent = null;
            for (DeployAppSchema.SpecComponent component : schema.getSpec().getComponents()) {
                DeployAppRevisionName revision = DeployAppRevisionName.valueOf(component.getRevisionName());
                if (revision.getComponentType().equals(componentType)
                        && revision.getComponentName().equals(componentName)) {
                    currentComponent = component;
                    hitRevisionSet.add(component.getRevisionName());
                    break;
                }
            }

            // ????????????????????????????????????????????????????????????
            if (currentComponent == null) {
                String componentConfiguration = packageOption.getString("componentConfiguration");
                if (StringUtils.isNotEmpty(componentConfiguration)) {
                    currentComponent = SchemaUtil.toSchema(DeployAppSchema.SpecComponent.class, componentConfiguration);
                    enrichSpecComponent(req, schema, hitRevisionSet, componentType, componentName, currentComponent);
                }
            }

            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (currentComponent == null && !req.isComponentPackageConfigurationFirst()) {
                String typeId = new DeployConfigTypeId(componentType).toString();
                List<DeployConfigDO> records = deployConfigService.list(DeployConfigQueryCondition.builder()
                        .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                        .appId("")
                        .typeId(typeId)
                        .enabled(true)
                        .build());
                DeployConfigDO best = deployConfigService.findBestConfigInRecordsByGeneralType(
                        records, req.getClusterId(), req.getNamespaceId(), req.getStageId());
                currentComponent = SchemaUtil.toSchema(DeployAppSchema.SpecComponent.class, best.getConfig());
                enrichSpecComponent(req, schema, hitRevisionSet, componentType, componentName, currentComponent);
            }

            // ??????????????????????????????????????????????????????????????????????????????????????????
            if (currentComponent == null) {
                continue;
            }

            // ?????????????????? SpecComponent ??????????????????
            switch (componentType) {
                case K8S_MICROSERVICE:
                case K8S_JOB:
                    // parameterValues Binder
                    JSONArray jsonBinderParameterValues = packageOption.getJSONArray("binderParameterValues");
                    if (jsonBinderParameterValues != null) {
                        List<ParamBinderDTO> parameterValues = jsonBinderParameterValues
                                .toJavaList(ParamBinderDTO.class);
                        for (ParamBinderDTO item : parameterValues) {
                            String inputName = item.getDataInputName();

                            // defaultValue ????????????????????????????????????????????? (????????? ApplicationConfiguration ????????????)
                            // ??????????????????????????????????????????????????????????????????????????? spec.env.$inputName
                            ComponentTypeEnum dependencyComponentType = item.getComponentType();
                            String dependencyComponentName = item.getComponentName();
                            String dependencyOutputName = item.getDataOutputName();
                            String defaultValue = item.getParamDefaultValue();
                            if (StringUtils.isNotEmpty(defaultValue)) {
                                boolean findFlag = false;
                                for (DeployAppSchema.ParameterValue componentParameterValue
                                        : currentComponent.getParameterValues()) {
                                    if (componentParameterValue.getName().equals(inputName)) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag) {
                                    currentComponent.getParameterValues().add(DeployAppSchema.ParameterValue.builder()
                                            .name(inputName)
                                            .value(defaultValue)
                                            .build());
                                }
                            } else if (dependencyComponentType != null
                                    && StringUtils.isNotEmpty(dependencyComponentName)
                                    && StringUtils.isNotEmpty(dependencyOutputName)) {
                                String defaultDataOutputName = generateDataOutputName(
                                        dependencyComponentType, dependencyComponentName, dependencyOutputName);
                                boolean findFlag = false;
                                for (DeployAppSchema.DataInput componentDataInput : currentComponent.getDataInputs()) {
                                    if (componentDataInput.getValueFrom().getDataOutputName()
                                            .equals(defaultDataOutputName)) {
                                        findFlag = true;
                                        break;
                                    }
                                }
                                if (!findFlag) {
                                    currentComponent.getDataInputs().add(DeployAppSchema.DataInput.builder()
                                            .valueFrom(DeployAppSchema.DataInputValueFrom.builder()
                                                    .dataOutputName(defaultDataOutputName)
                                                    .build())
                                            .toFieldPaths(Collections.singletonList(
                                                    String.format("spec.env.%s", inputName)))
                                            .build());
                                }
                            }
                        }
                    }

                    // dependencies Binder
                    JSONArray jsonBinderDependencies = packageOption.getJSONArray("binderDependencies");
                    if (jsonBinderDependencies != null) {
                        List<String> dependencies = jsonBinderDependencies.toJavaList(String.class);
                        // TODO
                    }

                    // traits Binder
                    JSONArray jsonBinderTraits = packageOption.getJSONArray("binderTraits");
                    if (jsonBinderTraits != null) {
                        List<TraitBinderDTO> traits = jsonBinderTraits.toJavaList(TraitBinderDTO.class);
                        // TODO
                    }
                    break;
                default:
                    break;
            }
        }

        // ???????????????????????????????????? components
        List<String> removedRevisionName = new ArrayList<>();
        for (DeployAppSchema.SpecComponent component : schema.getSpec().getComponents()) {
            String revisionName = component.getRevisionName();
            DeployAppRevisionName revision = DeployAppRevisionName.valueOf(component.getRevisionName());
            if (!revision.getComponentType().equals(ComponentTypeEnum.RESOURCE_ADDON)
                    && !hitRevisionSet.contains(revisionName)) {
                removedRevisionName.add(revisionName);
            }
        }
        schema.getSpec().getComponents().removeIf(item -> removedRevisionName.contains(item.getRevisionName()));
    }

    /**
     * ??? SpecComponent ????????????
     *
     * @param req              ????????????
     * @param schema           ?????? Schema
     * @param hitRevisionSet   ??????????????? revision ?????? (?????????????????????)
     * @param componentType    ????????????
     * @param componentName    ????????????
     * @param currentComponent ???????????? SpecComponent
     */
    private void enrichSpecComponent(
            ApplicationConfigurationGenerateReq req, DeployAppSchema schema, Set<String> hitRevisionSet,
            ComponentTypeEnum componentType, String componentName, DeployAppSchema.SpecComponent currentComponent) {
        String revisionName = DeployAppRevisionName.builder()
                .componentType(componentType)
                .componentName(componentName)
                .version(DefaultConstant.AUTO_VERSION)
                .build()
                .revisionName();
        currentComponent.setRevisionName(revisionName);
        deployConfigService.enrichComponentScopes(DeployConfigGenerateReq.builder()
                .clusterId(req.getClusterId())
                .namespaceId(req.getNamespaceId())
                .stageId(req.getStageId())
                .build(), currentComponent);
        schema.getSpec().getComponents().add(currentComponent);
        hitRevisionSet.add(revisionName);
    }

    /**
     * ?????? component ?????????????????? dataOutputName
     *
     * @param componentType ????????????
     * @param componentName ????????????
     * @param name          ??????
     * @return dataOutputName
     */
    private static String generateDataOutputName(ComponentTypeEnum componentType, String componentName, String name) {
        return String.format("%s.%s.dataOutputs.%s", componentType, componentName, name);
    }

    @Override
    public Long releaseCustomAddonMeta(AppPackageReleaseReq req) {
        long appPackageId = req.getAppPackageId();
        AppPackageQueryCondition condition = AppPackageQueryCondition.builder()
                .id(appPackageId)
                .withBlobs(false)
                .build();
        AppPackageDO appPackageDO = appPackageRepository.getByCondition(condition);
        Pagination<DeployAppBO> deployAppBOList = deployAppService.list(DeployAppQueryCondition.builder()
                .appPackageId(appPackageId)
                .deployStatus(DeployAppStateEnum.SUCCESS)
                .appId(appPackageDO.getAppId())
                .build(), true);
        ObjectUtil.checkNull(CheckNullObject.builder()
                .actionName("releaseCustomAddonMeta")
                .objectName("deployAppBOList")
                .checkObject(deployAppBOList)
                .build());
        DeployAppBO deployAppBO = deployAppBOList.getItems().get(0);
        ObjectUtil.checkNull(CheckNullObject.builder()
                .actionName("releaseCustomAddonMeta")
                .objectName("deployApp")
                .checkObject(deployAppBO)
                .build());
        DeployAppDO order = deployAppBO.getOrder();
        Map<String, String> attrMap = deployAppBO.getAttrMap();
        if (order == null || CollectionUtils.isEmpty(attrMap)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("actionName=%s|%s is NULL!", "releaseCustomAddonMeta", "order or attrMap"));
        }
        CustomAddonMetaDO customAddonMetaDO = CustomAddonMetaDO.builder()
                .addonId(req.getAddonId())
                .addonVersion(req.getAddonVersion())
                .addonType(AddonTypeEnum.CUSTOM.name())
                .addonSchema(generateCustomAddonSchema(req, order, attrMap))
                .build();
        customAddonMetaRepository.insert(customAddonMetaDO);
        return customAddonMetaDO.getId();
    }

    @Override
    public int delete(AppPackageQueryCondition condition) {
        return appPackageRepository.deleteByCondition(condition);
    }

    private String generateCustomAddonSchema(
            AppPackageReleaseReq req, DeployAppDO deployApp, Map<String, String> attrMap) {
        String addonName = ComponentTypeEnum.CUSTOM_ADDON.name().concat(".").concat(req.getAddonId());
        String addonVersion = req.getAddonVersion();
        String uuId = UUID.randomUUID().toString();

        CustomAddonSchema customAddonSchema = new CustomAddonSchema();
        customAddonSchema.setApiVersion(CustomComponentConstant.API_VERSION);
        customAddonSchema.setKind(CustomComponentConstant.KIND);

        generateCustomMeta(customAddonSchema, addonName);

        CustomWorkloadResource customWorkloadResource = new CustomWorkloadResource();
        customWorkloadResource.setApiVersion(CustomComponentConstant.WORKLOAD_API_VERSION);
        customWorkloadResource.setKind(CustomComponentConstant.WORKLOAD_KIND);
        genCustomSpecWorkloadMeta(customWorkloadResource, addonName, addonVersion, uuId);
        genCustomSpecWorkloadSpec(customWorkloadResource, req, deployApp, attrMap, addonName, uuId);

        Spec spec = new Spec();
        spec.setWorkload(customWorkloadResource);
        customAddonSchema.setSpec(spec);

        return SchemaUtil.toYamlMapStr(customAddonSchema);
    }

    private void generateCustomMeta(CustomAddonSchema customAddonSchema, String addonName) {
        MetaData metaData = new MetaData();
        metaData.setName(addonName);
        customAddonSchema.setMetadata(metaData);
    }

    private void genCustomSpecWorkloadMeta(
            CustomWorkloadResource customWorkloadResource, String addonName, String addonVersion, String uuId) {
        WorkloadResource.MetaData workloadMeta = new WorkloadResource.MetaData();
        workloadMeta.setName(addonName);
        JSONObject labels = (JSONObject) workloadMeta.getLabels();
        labels.put("labels.appmanager.oam.dev/namespaceId", CustomComponentConstant.NAMESPACE_TEMPLATE);
        labels.put("labels.appmanager.oam.dev/appId", CustomComponentConstant.APP_ID_TEMPLATE);
        labels.put("labels.appmanager.oam.dev/componentName", addonName.concat("+").concat(uuId));
        JSONObject workloadMetaAnnotations = (JSONObject) workloadMeta.getAnnotations();
        workloadMetaAnnotations.put("annotations.appmanager.oam.dev/version", addonVersion);
        customWorkloadResource.setMetadata(workloadMeta);
    }

    private void genCustomSpecWorkloadSpec(
            CustomWorkloadResource customWorkloadResource, AppPackageReleaseReq req, DeployAppDO deployApp,
            Map<String, String> attrMap, String addonName, String uuId) {
        DeployAppSchema deployAppSchema = SchemaUtil.toSchema(DeployAppSchema.class,
                attrMap.get(DeployAppAttrTypeEnum.APP_CONFIGURATION.toString()));
        CustomAddonWorkloadSpec customAddonWorkloadSpec = new CustomAddonWorkloadSpec();
        customAddonWorkloadSpec.setAppPackageId(req.getAppPackageId());
        customAddonWorkloadSpec.setParameterValues(req.getParameterValues());
        genApplicationConfig(customAddonWorkloadSpec, deployAppSchema, addonName, uuId);

        customWorkloadResource.setSpec(customAddonWorkloadSpec);
    }

    private void genApplicationConfig(
            CustomAddonWorkloadSpec customAddonWorkloadSpec, DeployAppSchema deployAppSchema,
            String addonName, String uuId) {
        deployAppSchema.getMetadata().getAnnotations().setAppId(CustomComponentConstant.APP_ID_TEMPLATE);
        deployAppSchema.getMetadata().setName(addonName.concat("+").concat(uuId));
        customAddonWorkloadSpec.setApplicationConfig(deployAppSchema);
    }

    /**
     * ???????????? appId ?????????????????????
     *
     * @param appId ?????? ID
     * @return ?????????????????????
     */
    @Override
    public String getNextVersion(String appId) {
        Pagination<AppPackageDO> existAppPackages = list(AppPackageQueryCondition
                .builder()
                .appId(appId)
                .build());
        String version;
        if (!existAppPackages.isEmpty()) {
            version = existAppPackages.getItems().get(0).getPackageVersion();
        } else {
            version = DefaultConstant.INIT_VERSION;
        }
        return VersionUtil.buildVersion(VersionUtil.buildNextPatch(version));
    }
}

