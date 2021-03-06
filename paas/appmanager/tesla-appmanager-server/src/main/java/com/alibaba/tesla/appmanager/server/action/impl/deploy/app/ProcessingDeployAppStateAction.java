package com.alibaba.tesla.appmanager.server.action.impl.deploy.app;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.AppFlowVariableKey;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.constants.TraitRuntimeConstant;
import com.alibaba.tesla.appmanager.common.enums.*;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.InstanceIdUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.common.util.StringUtil;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.server.action.DeployAppStateAction;
import com.alibaba.tesla.appmanager.server.dag.helper.DeployAppHelper;
import com.alibaba.tesla.appmanager.server.dag.nodes.DeployAppPreNode;
import com.alibaba.tesla.appmanager.server.dag.scenes.DeployAppComponentRunnerScene;
import com.alibaba.tesla.appmanager.server.event.deploy.DeployAppEvent;
import com.alibaba.tesla.appmanager.server.event.loader.DeployAppStateActionLoadedEvent;
import com.alibaba.tesla.appmanager.server.factory.JinjaFactory;
import com.alibaba.tesla.appmanager.server.repository.AppPackageComponentRelRepository;
import com.alibaba.tesla.appmanager.server.repository.AppPackageRepository;
import com.alibaba.tesla.appmanager.server.repository.ComponentPackageRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageComponentRelQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppPackageQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.ComponentPackageQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.RtAppInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.*;
import com.alibaba.tesla.appmanager.server.service.deploy.DeployAppService;
import com.alibaba.tesla.appmanager.server.service.rtappinstance.RtAppInstanceService;
import com.alibaba.tesla.dag.api.DagApiService;
import com.alibaba.tesla.dag.api.DagCreateEdge;
import com.alibaba.tesla.dag.api.DagCreateNode;
import com.alibaba.tesla.dag.model.domain.dagnode.DagInstNodeType;
import com.alibaba.tesla.dag.services.DagInstService;
import com.hubspot.jinjava.Jinjava;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * App ???????????? State ?????? Action - PROCESSING
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service("ProcessingDeployAppStateAction")
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class ProcessingDeployAppStateAction implements DeployAppStateAction, ApplicationRunner {

    private static final DeployAppStateEnum STATE = DeployAppStateEnum.PROCESSING;

    private static final Integer DEFAULT_RUN_TIMEOUT = 3600;

    private static final String DEFAULT_EDGE_EXPRESSION = "1==1";

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private AppPackageRepository appPackageRepository;

    @Autowired
    private ComponentPackageRepository componentPackageRepository;

    @Autowired
    private AppPackageComponentRelRepository appPackageComponentRelRepository;

    @Autowired
    private DagApiService dagApiService;

    @Autowired
    private DagInstService dagInstService;

    @Autowired
    private DeployAppService deployAppService;

    @Autowired
    private RtAppInstanceService rtAppInstanceService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        publisher.publishEvent(new DeployAppStateActionLoadedEvent(
                this, STATE.toString(), this.getClass().getSimpleName()));
    }

    /**
     * ??????????????????
     *
     * @param order   ????????????
     * @param attrMap ????????????
     */
    @Override
    public void run(DeployAppDO order, Map<String, String> attrMap) {
        Long deployAppId = order.getId();
        Long appPackageId = order.getAppPackageId();
        String creator = order.getDeployCreator();
        AppPackageQueryCondition condition = AppPackageQueryCondition.builder()
                .id(appPackageId)
                .withBlobs(false)
                .build();
        AppPackageDO appPackageDO = appPackageRepository.getByCondition(condition);
        if (appPackageDO == null) {
            throw new AppException(AppErrorCode.DEPLOY_ERROR,
                    String.format("cannot find app package record in db|deployAppId=%d|appPackageId=%s",
                            deployAppId, appPackageId));
        }
        List<AppPackageComponentRelDO> rels = appPackageComponentRelRepository.selectByCondition(
                AppPackageComponentRelQueryCondition.builder().appPackageId(appPackageDO.getId()).build());
        List<ComponentPackageDO> componentPackages = new ArrayList<>();
        for (AppPackageComponentRelDO rel : rels) {
            Long componentPackageId = rel.getComponentPackageId();
            assert componentPackageId != null;
            ComponentPackageDO componentPackageDO = componentPackageRepository.getByCondition(
                    ComponentPackageQueryCondition.builder().id(componentPackageId).withBlobs(true).build()
            );
            assert componentPackageDO != null;
            componentPackages.add(componentPackageDO);
        }

        // ????????????????????? DAG
        try {
            Long deployProcessId = deployComponents(order, attrMap, componentPackages, creator);
            log.info("deploy app process has triggered|deployProcessId={}|deployAppId={}|appPackageId={}",
                    deployProcessId, deployAppId, appPackageId);
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                    String.format("unknown error processing deploy app order|order=%s|appPackage=%s|creator=%s|" +
                                    "exception=%s", JSON.toJSONString(order), JSON.toJSONString(appPackageDO), creator,
                            ExceptionUtils.getStackTrace(e)));
        }
        publisher.publishEvent(new DeployAppEvent(this, DeployAppEventEnum.PROCESS_FINISHED, order.getId()));
    }

    /**
     * ?????? order ?????? ApplicationConfiguration Yaml ???????????? Component ?????????????????????????????? DAG ???????????????
     * <p>
     * ?????? DAG ????????? dagInstId
     *
     * @param order             ??????
     * @param attrMap           ????????????
     * @param componentPackages ???????????????
     * @return DAG ???????????? ID
     */
    private Long deployComponents(
            DeployAppDO order, Map<String, String> attrMap,
            List<ComponentPackageDO> componentPackages, String creator) throws Exception {
        Long deployAppId = order.getId();
        DeployAppSchema configuration = SchemaUtil.toSchema(DeployAppSchema.class,
                attrMap.get(DeployAppAttrTypeEnum.APP_CONFIGURATION.toString()));
        JSONObject globalParameters = new JSONObject();
        for (DeployAppSchema.ParameterValue parameterValue : configuration.getSpec().getParameterValues()) {
            String name = StringUtil.globalParamName(parameterValue.getName());
            Object value = parameterValue.getValue();
            DeployAppHelper.recursiveSetParameters(globalParameters, null, Arrays.asList(name.split("\\.")), value,
                    ParameterValueSetPolicy.OVERWRITE_ON_CONFILICT);
        }

        Map<String, List<String>> providerMapping = new HashMap<>();
        Map<String, String> componentMapping = new HashMap<>();
        Map<String, DeployAppSchema.SpecComponent> specComponentMapping = new HashMap<>();
        initProviderMapping(configuration, providerMapping);
        initComponentMapping(configuration, componentMapping, specComponentMapping);

        // ?????? deploy app schema ???????????? DAG ???????????????
        List<DagCreateNode> nodes = createSceneNodes(configuration);
        Set<DagCreateEdge> edges = new HashSet<>();
        configuration.getSpec().getComponents().forEach(component -> {
            DeployAppRevisionName componentRevision = DeployAppRevisionName.valueOf(component.getRevisionName());
            String componentId = component.getUniqueId();
            String mirrorComponentId = component.getMirrorUniqueId();

            // mirror -> self, ??????????????????????????? mirror?????? self
            edges.add(DagCreateEdge.builder()
                    .sourceNodeId(mirrorComponentId)
                    .targetNodeId(componentId)
                    .expression(DEFAULT_EDGE_EXPRESSION)
                    .build());

            List<DeployAppSchema.DataInput> componentDataInputs = component.getDataInputs();
            // ?????????????????????????????? mirror component ???
            addEdgeByDataInputs(providerMapping, edges, mirrorComponentId, componentDataInputs);
            String lastTraitId = null;
            Set<String> traitUsedSet = new HashSet<>();
            for (int i = 0; i < component.getTraits().size(); i++) {
                DeployAppSchema.SpecComponentTrait trait = component.getTraits().get(i);
                String traitId = trait.getUniqueId(componentRevision);

                // ?????? trait ??????
                if (traitUsedSet.contains(traitId)) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("duplicate trait %s", traitId));
                }
                traitUsedSet.add(traitId);

                List<DeployAppSchema.DataInput> traitDataInputs = trait.getDataInputs();
                addEdgeByDataInputs(providerMapping, edges, traitId, traitDataInputs);
                // ?????? trait ???????????? runtime???????????? pre
                if (StringUtils.isEmpty(trait.getRuntime())) {
                    trait.setRuntime(TraitRuntimeConstant.RUNTIME_PRE);
                }
                // ?????? trait??????????????????????????? component ??????????????? pre/post ??????????????????
                switch (trait.getRuntime()) {
                    case TraitRuntimeConstant.RUNTIME_PRE:
                        // pre trait ?????? mirror -> trait -> self ???????????????
                        edges.add(DagCreateEdge.builder()
                                .sourceNodeId(mirrorComponentId)
                                .targetNodeId(traitId)
                                .expression(DEFAULT_EDGE_EXPRESSION)
                                .build());
                        edges.add(DagCreateEdge.builder()
                                .sourceNodeId(traitId)
                                .targetNodeId(componentId)
                                .expression(DEFAULT_EDGE_EXPRESSION)
                                .build());
                        // ????????????????????? pre ??? trait????????????????????? pre trait ?????????????????????????????? pre trait ????????????
                        if (!StringUtils.isEmpty(lastTraitId)) {
                            edges.add(DagCreateEdge.builder()
                                    .sourceNodeId(lastTraitId)
                                    .targetNodeId(traitId)
                                    .expression(DEFAULT_EDGE_EXPRESSION)
                                    .build());
                        }
                        lastTraitId = traitId;
                        break;
                    case TraitRuntimeConstant.RUNTIME_POST:
                        // post trait ????????? self -> trait ???????????????
                        edges.add(DagCreateEdge.builder()
                                .sourceNodeId(componentId)
                                .targetNodeId(traitId)
                                .expression(DEFAULT_EDGE_EXPRESSION)
                                .build());
                        break;
                    default:
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                "trait runtime field can be only in pre/post");
                }
            }
            component.getDependencies().forEach(dependency -> {
                String source = componentMapping.get(dependency.getComponent());
                DeployAppSchema.SpecComponent specSource = specComponentMapping.get(dependency.getComponent());
                if (source == null || specSource == null) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("invalid dependnecy component %s", dependency.getComponent()));
                }
                String condition = dependency.getCondition();
                if (StringUtils.isEmpty(condition)) {
                    condition = DEFAULT_EDGE_EXPRESSION;
                }
                edges.add(DagCreateEdge.builder()
                        .sourceNodeId(source)
                        .targetNodeId(mirrorComponentId)
                        .expression(condition)
                        .build());

                // ?????????????????? source ????????? runtime post ??? trait ?????????????????? mirror ??????
//                for (int i = 0; i < specSource.getTraits().size(); i++) {
//                    DeployAppSchema.SpecComponentTrait trait = specSource.getTraits().get(i);
//                    if (TraitRuntimeConstant.RUNTIME_POST.equals(trait.getRuntime())) {
//                        String traitId = trait.getUniqueId(componentRevision);
//                        edges.add(DagCreateEdge.builder()
//                                .sourceNodeId(traitId)
//                                .targetNodeId(mirrorComponentId)
//                                .expression(condition)
//                                .build());
//                    }
//                }
            });
        });
        String dagName = String.format("%d_%s_%s_%d", deployAppId, configuration.getMetadata().getName(),
                configuration.getMetadata().getAnnotations().getAppPackageVersion(), System.currentTimeMillis());
        log.info("prepare to create deploy app dag|deployAppId={}|dagName={}|nodes={}|edges={}", deployAppId, dagName,
                JSONArray.toJSON(nodes.stream()
                        .map(DagCreateNode::getNodeId)
                        .collect(Collectors.toList())),
                JSONArray.toJSON(edges.stream()
                        .map(p -> String.format("%s->%s", p.getSourceNodeId(), p.getTargetNodeId()))
                        .collect(Collectors.toList())));
        Long dagId = dagApiService.create(dagName, DagCreateNode.builder()
                .params(new HashMap<>())
                .name(DeployAppPreNode.name)
                .nodeId(DeployAppPreNode.name)
                .type(DagInstNodeType.NODE)
                .runTimeout(DEFAULT_RUN_TIMEOUT)
                .build(), null, nodes, new ArrayList<>(edges));
        log.info("create deploy app dag success|deployAppId={}|dagId={}|dagName={}", deployAppId, dagId, dagName);

        // ???????????????????????????????????????????????????????????? appInstanceId????????????????????????????????????????????? appInstanceId
        // TODO: ?????????????????? Namespace ??? app instance ??????
        String ownerReferenceStr = "";
        String appInstanceName = configuration.getMetadata().getAnnotations().getAppInstanceName();
        for (DeployAppSchema.SpecComponent specComponent : configuration.getSpec().getComponents()) {
            Jinjava jinjava = JinjaFactory.getJinjava();
            String componentClusterId = jinjava.render(specComponent.getClusterId(), globalParameters);
            String componentNamespaceId = jinjava.render(specComponent.getNamespaceId(), globalParameters);
            String componentStageId = jinjava.render(specComponent.getStageId(), globalParameters);
            String appInstanceId = InstanceIdUtil.genAppInstanceId(order.getAppId(), componentClusterId,
                    componentNamespaceId, componentStageId);
            if (StringUtils.isEmpty(appInstanceName)) {
                appInstanceName = appInstanceId;
            }
            // ?????? appmeta / developmentmeta ??????????????? INTERNAL_ADDON component, ????????????????????????????????????
            DeployAppRevisionName revision = DeployAppRevisionName.valueOf(specComponent.getRevisionName());
            if (ComponentTypeEnum.INTERNAL_ADDON.equals(revision.getComponentType())
                    && ("appmeta".equals(revision.getComponentName())
                    || "developmentmeta".equals(revision.getComponentName()))) {
                continue;
            }
            RtAppInstanceDO appInstance = rtAppInstanceService.getOrCreate(RtAppInstanceQueryCondition.builder()
                    .appId(order.getAppId())
                    .clusterId(componentClusterId)
                    .namespaceId(componentNamespaceId)
                    .stageId(componentStageId)
                    .build(), appInstanceId, appInstanceName, order.getPackageVersion());
            if (appInstance != null && StringUtils.isNotEmpty(appInstance.getOwnerReference())) {
                ownerReferenceStr = appInstance.getOwnerReference();
            }
        }

        // ?????? DAG ???????????????
        JSONObject variables = new JSONObject();
        variables.put(DefaultConstant.DAG_TYPE, DagTypeEnum.DEPLOY_APP.toString());
        variables.put(AppFlowVariableKey.DEPLOY_ID, deployAppId);
        variables.put(AppFlowVariableKey.DAG_ID, dagId);
        variables.put(AppFlowVariableKey.DAG_NAME, dagName);
        variables.put(AppFlowVariableKey.APP_ID, order.getAppId());
        variables.put(AppFlowVariableKey.APP_INSTANCE_NAME, appInstanceName);
        variables.put(AppFlowVariableKey.CLUSTER_ID, order.getClusterId());
        variables.put(AppFlowVariableKey.NAMESPACE_ID, order.getNamespaceId());
        variables.put(AppFlowVariableKey.STAGE_ID, order.getStageId());
        variables.put(AppFlowVariableKey.CONFIGURATION, attrMap.get(DeployAppAttrTypeEnum.APP_CONFIGURATION.toString()));
        variables.put(AppFlowVariableKey.COMPONENT_PACKAGES, JSONArray.toJSONString(componentPackages));
        variables.put(AppFlowVariableKey.OWNER_REFERENCE, ownerReferenceStr);
        variables.put(AppFlowVariableKey.CREATOR, creator);

        // ??????????????????
        deployAppService.updateAttr(order.getId(), DeployAppAttrTypeEnum.GLOBAL_VARIABLES.toString(),
                variables.toJSONString());
        deployAppService.updateAttr(order.getId(), DeployAppAttrTypeEnum.GLOBAL_PARAMS.toString(), "{}");

        // ??????
        Long dagInstId = dagInstService.start(dagName, variables, true);
        log.info("trigger deploy app dag success|deployAppId={}|appId={}|clusterId={}|namespaceId={}|stageId={}|" +
                        "dagInstId={}|dagId={}|dagName={}|nodes={}|edges={}", deployAppId, order.getAppId(),
                order.getClusterId(), order.getNamespaceId(), order.getStageId(), dagInstId, dagId, dagName,
                JSONArray.toJSON(nodes.stream()
                        .map(DagCreateNode::getNodeId)
                        .collect(Collectors.toList())),
                JSONArray.toJSON(edges.stream()
                        .map(p -> String.format("%s->%s", p.getSourceNodeId(), p.getTargetNodeId()))
                        .collect(Collectors.toList())));
        order.setDeployProcessId(dagInstId);
        deployAppService.update(order);
        return dagInstId;
    }

    /**
     * ???????????????????????? component ??? trait ?????????????????? DAG ???????????? List
     *
     * @param schema ????????? Schema
     * @return List of dag node
     */
    private List<DagCreateNode> createSceneNodes(DeployAppSchema schema) {
        List<DagCreateNode> nodes = new ArrayList<>();
        String runnerSceneName = DeployAppComponentRunnerScene.name;
        schema.getSpec().getComponents().forEach(component -> {
            DeployAppRevisionName componentRevision = DeployAppRevisionName.valueOf(component.getRevisionName());
            // ???????????? Component ??????
            nodes.add(DagCreateNode.builder()
                    .params(new HashMap<>())
                    .name(runnerSceneName)
                    .nodeId(component.getUniqueId())
                    .type(DagInstNodeType.DAG)
                    .runTimeout(DEFAULT_RUN_TIMEOUT)
                    .build());
            // ?????? Trait ??????
            component.getTraits().forEach(trait -> {
                String traitId = trait.getUniqueId(componentRevision);
                nodes.add(DagCreateNode.builder()
                        .params(new HashMap<>())
                        .name(runnerSceneName)
                        .nodeId(traitId)
                        .type(DagInstNodeType.DAG)
                        .runTimeout(DEFAULT_RUN_TIMEOUT)
                        .build());
            });
            // ???????????? Component ??????
            nodes.add(DagCreateNode.builder()
                    .params(new HashMap<>())
                    .name(runnerSceneName)
                    .nodeId(component.getMirrorUniqueId())
                    .type(DagInstNodeType.DAG)
                    .runTimeout(DEFAULT_RUN_TIMEOUT)
                    .build());
        });
        return nodes;
    }

    /**
     * ?????? dataInputs ?????????????????? DAG Edge ??????
     *
     * @param outputMapping ????????????????????? Map (key: variable_name, value: component_id list)
     * @param edges         DAG ?????????
     * @param currentNodeId ???????????? ID
     * @param dataInputs    ????????????????????????????????????
     */
    private void addEdgeByDataInputs(
            Map<String, List<String>> outputMapping, Set<DagCreateEdge> edges, String currentNodeId,
            List<DeployAppSchema.DataInput> dataInputs) {
        dataInputs.forEach(dataInput -> {
            if (dataInput.getValueFrom() == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("cannot find valueFrom field in dataInput field|dataInput=%s",
                                JSONObject.toJSONString(dataInput)));
            }
            String variable = dataInput.getValueFrom().getDataOutputName();
            List<String> providers = outputMapping.getOrDefault(variable, new ArrayList<>());
            if (providers.size() == 0) {
                log.info("cannot find the api for variable {}, currentNodeId={}", variable, currentNodeId);
            } else {
                providers.forEach(provider -> edges.add(DagCreateEdge.builder()
                        .sourceNodeId(provider)
                        .targetNodeId(currentNodeId)
                        .expression(DEFAULT_EDGE_EXPRESSION)
                        .build()));
            }
        });
    }

    /**
     * ????????? Variable ??? Component ?????????????????????
     *
     * @param schema          ????????????
     * @param providerMapping ????????????????????? Map (key: variable_name, value: component_id list)
     */
    private void initProviderMapping(DeployAppSchema schema, Map<String, List<String>> providerMapping) {
        schema.getSpec().getComponents().forEach(component -> {
            DeployAppRevisionName componentRevision = DeployAppRevisionName.valueOf(component.getRevisionName());
            String componentId = component.getUniqueId();
            component.getDataOutputs().forEach(dataOutput -> {
                String variable = dataOutput.getName();
                providerMapping.putIfAbsent(variable, new ArrayList<>());
                providerMapping.get(variable).add(componentId);
            });
            component.getTraits().forEach(trait -> {
                if (TraitRuntimeConstant.RUNTIME_POST.equals(trait.getRuntime())) {
                    trait.getDataOutputs().forEach(dataOutput -> {
                        String variable = dataOutput.getName();
                        providerMapping.putIfAbsent(variable, new ArrayList<>());
                        providerMapping.get(variable).add(trait.getUniqueId(componentRevision));
                    });
                }
            });
        });
    }


    /**
     * ?????? component ($componentType|$componentName) ??? component node id ?????????
     *
     * @param schema               DeployAppSchema ????????????
     * @param componentMapping     ?????? Component Node ID ?????? Map
     * @param specComponentMapping ?????? SpecComponent ?????? Map
     */
    private void initComponentMapping(
            DeployAppSchema schema, Map<String, String> componentMapping,
            Map<String, DeployAppSchema.SpecComponent> specComponentMapping) {
        schema.getSpec().getComponents().forEach(component -> {
            DeployAppRevisionName componentRevision = DeployAppRevisionName
                    .valueOf(component.getRevisionName());
            String componentId = component.getUniqueId();
            String key = String.format("%s|%s", componentRevision.getComponentType(),
                    componentRevision.getComponentName());
            componentMapping.put(key, componentId);
            specComponentMapping.put(key, component);
        });
    }
}
