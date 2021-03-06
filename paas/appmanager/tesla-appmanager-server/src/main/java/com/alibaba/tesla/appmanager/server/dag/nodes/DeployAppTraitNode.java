package com.alibaba.tesla.appmanager.server.dag.nodes;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.AppFlowParamKey;
import com.alibaba.tesla.appmanager.common.constants.AppFlowVariableKey;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.*;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.DateUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource;
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq;
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes;
import com.alibaba.tesla.appmanager.domain.schema.ComponentSchema;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory;
import com.alibaba.tesla.appmanager.server.dag.helper.ComponentTrait;
import com.alibaba.tesla.appmanager.server.dag.helper.DeployAppHelper;
import com.alibaba.tesla.appmanager.server.repository.condition.DeployComponentQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.DeployComponentDO;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployAppService;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployComponentService;
import com.alibaba.tesla.appmanager.server.service.deploy.business.DeployComponentBO;
import com.alibaba.tesla.appmanager.trait.Trait;
import com.alibaba.tesla.appmanager.trait.TraitFactory;
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler;
import com.alibaba.tesla.dag.common.BeanUtil;
import com.alibaba.tesla.dag.local.AbstractLocalNodeBase;
import com.alibaba.tesla.dag.model.domain.dagnode.DagInstNodeRunRet;
import com.hubspot.jinjava.Jinjava;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * ?????? App - ?????? Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
public class DeployAppTraitNode extends AbstractLocalNodeBase {

    private static final Long UNKNOWN_VALUE = -1L;

    @Override
    public DagInstNodeRunRet run() throws Exception {
        DeployAppService deployAppService = getDeployAppService();
        TraitFactory traitFactory = getTraitFactory();
        DeployComponentService deployComponentService = getDeployComponentService();
        GroovyHandlerFactory groovyHandlerFactory = getGroovyHandlerFactory();

        Long deployAppId = Long.valueOf(globalVariable.get(AppFlowVariableKey.DEPLOY_ID).toString());
        log.info("enter the execution process of DeployAppTraitNode|deployAppId={}|nodeId={}|" +
                "dagInstId={}", deployAppId, nodeId, dagInstId);
        String nodeId = fatherNodeId;
        assert !StringUtils.isEmpty(nodeId);
        DeployAppRevisionName revisionName = DeployAppRevisionName.valueOf(nodeId);
        String componentName = revisionName.getComponentName();

        // traitName ??????
        // ?????? componentName???MICROSERVICE~testservice~stage.flyadmin.alibaba.com
        // ?????????${componentType}~${componentName}~${traitName}
        String[] splitNames = componentName.split("~", 3);
        if (splitNames.length < 3) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("invalid component name %s in revisionName %s", componentName, nodeId));
        }
        String traitName = splitNames[2];

        // ??????????????? Trait ?????????????????? ComponentOptions???????????????????????????
        DeployAppSchema configuration = SchemaUtil.toSchema(DeployAppSchema.class,
                globalVariable.get(AppFlowVariableKey.CONFIGURATION).toString());
        JSONObject parameters = globalParams
                .getJSONObject(AppFlowParamKey.OVERWRITE_PARAMETER_VALUES).clone();
        ComponentTrait componentTrait = DeployAppHelper.findComponentTrait(nodeId, configuration);
        for (DeployAppSchema.ParameterValue parameterValue : componentTrait.getTrait().getParameterValues()) {
            String key = parameterValue.getName();
            Object value = parameterValue.getValue();
            DeployAppHelper.recursiveSetParameters(parameters, null, Arrays.asList(key.split("\\.")), value,
                    ParameterValueSetPolicy.OVERWRITE_ON_CONFILICT);
        }
        componentTrait.setComponent(DeployAppHelper.renderDeployAppComponent(parameters, componentTrait.getComponent()));
        componentTrait.setTrait(DeployAppHelper.renderDeployAppTrait(parameters, componentTrait.getTrait()));

        // ??????????????? ComponentSchema ??? Trait
        JSONObject traitSpec = DeployAppHelper.renderJsonObject(parameters, componentTrait.getTrait().getSpec());
        ComponentSchema componentSchema;
        Date start = new Date();
        try {
            WorkloadResource traitComponentWorkload;
            String componentRevision = componentTrait.getComponent().getRevisionName();
            String componentRevisionKey = AppFlowParamKey.componentSchemaMapKeyGenerator(componentRevision);
            if (componentTrait.getTrait().getRuntime().equals("pre")) {
                // ?????? trait ????????????????????? component schema ????????????
                String componentSchemaStr = deployAppService.getAttr(deployAppId, componentRevisionKey);
                if (StringUtils.isEmpty(componentSchemaStr)) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("cannot get component schema with revision %s in system",
                                    revisionName.revisionName()));
                }
                componentSchema = SchemaUtil.toSchema(ComponentSchema.class, componentSchemaStr);
            } else {
                // ?????? trait ??????????????????????????? component schema??????????????????????????????
                componentSchema = getStaticComponentSchema(componentTrait);
            }
            traitComponentWorkload = componentSchema.getSpec().getWorkload();

            // ????????? groovy handler ?????????????????????????????????????????????????????????????????? plugins ???????????????
            long startTimestamp = System.currentTimeMillis();
            TraitHandler traitHandler;
            try {
                traitHandler = groovyHandlerFactory
                        .get(TraitHandler.class, DynamicScriptKindEnum.TRAIT.toString(), traitName);
            } catch (AppException e) {
                if (AppErrorCode.INVALID_USER_ARGS.equals(e.getErrorCode())) {
                    traitHandler = null;
                } else {
                    throw e;
                }
            }

            // ?????? owner reference
            String ownerReference = globalVariable.getString(AppFlowVariableKey.OWNER_REFERENCE);
            if (StringUtils.isEmpty(ownerReference)) {
                ownerReference = "";
            }

            if (traitHandler != null) {
                log.info("prepare to run trait {}|deployAppId={}|nodeId={}|dagInstId={}|spec={}",
                        traitName, deployAppId, nodeId, dagInstId, JSONObject.toJSONString(traitSpec));
                TraitExecuteRes traitExecuteRes = traitHandler.execute(TraitExecuteReq.builder()
                        .name(traitName)
                        .spec(traitSpec)
                        .ref(traitComponentWorkload)
                        .component(componentTrait.getComponent())
                        .traitDefinition(traitFactory.newTraitDefinition(traitName))
                        .ownerReference(ownerReference)
                        .build());
                traitSpec = traitExecuteRes.getSpec();
            } else {
                log.info("prepare to run trait {}|deployAppId={}|nodeId={}|dagInstId={}|spec={}",
                        traitName, deployAppId, nodeId, dagInstId, JSONObject.toJSONString(traitSpec));
                Trait trait = traitFactory.newInstance(traitName, traitSpec, traitComponentWorkload);
                trait.setComponent(componentTrait.getComponent());
                trait.setOwnerReference(ownerReference);
                trait.execute();
                traitSpec = trait.getSpec();
            }
            log.info("trait {} has finished running|deployAppId={}|nodeId={}|dagInstId={}|cost={}|" +
                            "afterRunningSpec={}", traitName, deployAppId, nodeId, dagInstId,
                    DateUtil.costTime(startTimestamp), JSONObject.toJSONString(traitSpec));

            // ??????????????? trait??????????????? put ????????? component schema ??? global params ???????????????
            if (componentTrait.getTrait().getRuntime().equals("pre")) {
                deployAppService.updateAttr(deployAppId, componentRevisionKey,
                        SchemaUtil.toYamlMapStr(componentSchema));
            }

            // ???????????????????????? Trait ???????????? DeployComponent ??????
            Date end = new Date();
            DeployComponentDO subOrder = DeployComponentDO.builder()
                    .deployId(deployAppId)
                    .deployType(DeployComponentTypeEnum.TRAIT.toString())
                    .identifier(nodeId)
                    .appId(globalVariable.getString(AppFlowVariableKey.APP_ID))
                    .clusterId(componentTrait.getComponent().getClusterId())
                    .namespaceId(componentTrait.getComponent().getNamespaceId())
                    .stageId(componentTrait.getComponent().getStageId())
                    .gmtStart(start)
                    .gmtEnd(end)
                    .deployStatus(DeployComponentStateEnum.SUCCESS.toString())
                    .deployCreator(globalVariable.getString(AppFlowVariableKey.CREATOR))
                    .build();
            Map<DeployComponentAttrTypeEnum, String> attrMap = new HashMap<>();
            attrMap.put(DeployComponentAttrTypeEnum.TRAIT_SCHEMA, SchemaUtil.toYamlMapStr(traitSpec));
            if (traitComponentWorkload == null) {
                attrMap.put(DeployComponentAttrTypeEnum.TRAIT_COMPONENT_WORKLOAD, "");
            } else {
                attrMap.put(DeployComponentAttrTypeEnum.TRAIT_COMPONENT_WORKLOAD,
                        SchemaUtil.toYamlMapStr(traitComponentWorkload));
            }
            deployComponentService.create(subOrder, attrMap);
        } catch (Exception e) {
            // ???????????????????????? Trait ???????????? DeployComponent ??????
            Date end = new Date();
            DeployComponentDO subOrder = DeployComponentDO.builder()
                    .deployId(deployAppId)
                    .deployType(DeployComponentTypeEnum.TRAIT.toString())
                    .identifier(nodeId)
                    .appId(globalVariable.getString(AppFlowVariableKey.APP_ID))
                    .clusterId(componentTrait.getComponent().getClusterId())
                    .namespaceId(componentTrait.getComponent().getNamespaceId())
                    .stageId(componentTrait.getComponent().getStageId())
                    .gmtStart(start)
                    .gmtEnd(end)
                    .deployStatus(DeployComponentStateEnum.FAILURE.toString())
                    .deployCreator(globalVariable.getString(AppFlowVariableKey.CREATOR))
                    .build();
            Map<DeployComponentAttrTypeEnum, String> attrMap = new HashMap<>();
            attrMap.put(DeployComponentAttrTypeEnum.TRAIT_SCHEMA, SchemaUtil.toYamlMapStr(traitSpec));
            deployComponentService.create(subOrder, attrMap);
            throw e;
        }

        // ?????? dataOutput ???????????????????????????????????? set ????????????????????????
        Jinjava jinjava = new Jinjava();
        JSONObject finalParameters = globalParams.getJSONObject(AppFlowParamKey.OVERWRITE_PARAMETER_VALUES);
        List<DeployAppSchema.DataOutput> dataOutputs = componentTrait.getTrait().getDataOutputs();
        for (DeployAppSchema.DataOutput dataOutput : dataOutputs) {
            String fieldPath = dataOutput.getFieldPath();
            String name = dataOutput.getName();
            Object value;
            if (fieldPath.startsWith("{{")) {
                // Jinja ????????????
                if (!fieldPath.startsWith("{{ spec.")) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("invalid field path in dataOuput %s", JSONObject.toJSONString(dataOutput)));
                }
                fieldPath = fieldPath.replace("spec.", "");
                value = jinjava.render(fieldPath, traitSpec);
            } else {
                // JSONPath ????????????
                if (!fieldPath.startsWith("spec.")) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("invalid field path in dataOuput %s", JSONObject.toJSONString(dataOutput)));
                }
                fieldPath = fieldPath.replace("spec.", "");
                DocumentContext workloadContext = JsonPath.parse(JSONObject.toJSONString(traitSpec));
                value = workloadContext.read(DefaultConstant.JSONPATH_PREFIX + fieldPath);
            }
            DeployAppHelper.recursiveSetParameters(finalParameters, null, Arrays.asList(name.split("\\.")), value,
                    ParameterValueSetPolicy.OVERWRITE_ON_CONFILICT);
            log.info("dataOutput has put into overwrite parameters|name={}|value={}|deployAppId={}|fieldPath={}",
                    name, value, deployAppId, fieldPath);
        }
        return DagInstNodeRunRet.builder().build();
    }

    private ComponentSchema getStaticComponentSchema(ComponentTrait componentTrait) {
        DeployComponentService deployComponentService = BeanUtil.getBean(DeployComponentService.class);
        assert deployComponentService != null;

        Long deployAppId = Long.valueOf(globalVariable.get(AppFlowVariableKey.DEPLOY_ID).toString());
        DeployComponentQueryCondition condition = DeployComponentQueryCondition.builder()
                .deployAppId(deployAppId)
                .identifier(componentTrait.getComponent().getRevisionName())
                .build();
        List<DeployComponentBO> deployComponentList = deployComponentService.list(condition, true);
        assert deployComponentList.size() == 1;
        return SchemaUtil.toSchema(ComponentSchema.class,
                deployComponentList.get(0).getAttrMap().get(DeployComponentAttrTypeEnum.COMPONENT_SCHEMA.toString()));
    }

    public DeployAppService getDeployAppService() {
        return BeanUtil.getBean(DeployAppService.class);
    }

    public TraitFactory getTraitFactory() {
        return BeanUtil.getBean(TraitFactory.class);
    }

    public DeployComponentService getDeployComponentService() {
        return BeanUtil.getBean(DeployComponentService.class);
    }

    public GroovyHandlerFactory getGroovyHandlerFactory() {
        return BeanUtil.getBean(GroovyHandlerFactory.class);
    }
}
