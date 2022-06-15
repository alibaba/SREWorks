package com.alibaba.tesla.appmanager.meta.k8smicroservice.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigEnvId;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.LaunchDTO;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigUpdateReq;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.K8sMicroServiceMetaRepository;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition.K8sMicroserviceMetaQueryCondition;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.service.K8sMicroserviceMetaService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * K8S 微应用元信息服务
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Service
@Slf4j
public class K8SMicroserviceMetaServiceImpl implements K8sMicroserviceMetaService {

    @Autowired
    private K8sMicroServiceMetaRepository k8sMicroServiceMetaRepository;

    @Autowired
    private DeployConfigService deployConfigService;

    /**
     * 根据条件过滤微应用元信息列表
     *
     * @param condition 过滤条件
     * @return List
     */
    @Override
    public Pagination<K8sMicroServiceMetaDO> list(K8sMicroserviceMetaQueryCondition condition) {
        List<K8sMicroServiceMetaDO> metaList = k8sMicroServiceMetaRepository.selectByCondition(condition);
        return Pagination.valueOf(metaList, Function.identity());
    }

    /**
     * 根据主键 ID 获取微应用元信息
     *
     * @param id 微应用元信息主键 ID
     * @return K8sMicroServiceMetaDO
     */
    @Override
    public K8sMicroServiceMetaDO get(Long id) {
        return k8sMicroServiceMetaRepository.selectByPrimaryKey(id);
    }

    /**
     * 根据 appId + microServiceId + namespaceId + stageId 获取微应用元信息
     *
     * @param appId 应用 ID
     * @param microServiceId 微服务标识
     * @param namespaceId Namespace ID
     * @param stageId Stage ID
     * @return K8sMicroServiceMetaDO
     */
    @Override
    public K8sMicroServiceMetaDO getByMicroServiceId(
            String appId, String microServiceId, String namespaceId, String stageId) {
        K8sMicroserviceMetaQueryCondition condition = K8sMicroserviceMetaQueryCondition.builder()
                .microServiceId(microServiceId)
                .appId(appId)
                .namespaceId(namespaceId)
                .stageId(stageId)
                .withBlobs(true)
                .build();
        List<K8sMicroServiceMetaDO> metaList = k8sMicroServiceMetaRepository.selectByCondition(condition);
        if (metaList.size() > 0) {
            return metaList.get(0);
        } else {
            return null;
        }
    }

    private void refreshDeployConfig(K8sMicroServiceMetaDO metaDO) {
        LaunchDTO launchObject = metaDO.getLaunchObject();
        if (launchObject == null) {
            log.info("appId: " + metaDO.getAppId() + " launchObject is null, skip");
            return;
        }

        JSONObject configObject = new JSONObject();
        JSONArray parameterValues = new JSONArray();
        JSONArray traits = new JSONArray();
        JSONArray scopes = new JSONArray();

        JSONObject nsScopeObject = new JSONObject();
        JSONObject nsObject = new JSONObject();
        JSONObject nsSpecObject = new JSONObject();
        nsSpecObject.put("autoCreate", true);
        if (launchObject.getNamespaceResourceLimit() != null) {
            JSONObject resourceQuotaObject = new JSONObject();
            resourceQuotaObject.put("name", "sreworks-resource-limit");
            resourceQuotaObject.put("spec", launchObject.getNamespaceResourceLimit());
            nsSpecObject.put("resourceQuota", resourceQuotaObject);
        }
        nsObject.put("spec", nsSpecObject);
        nsObject.put("apiVersion", "core.oam.dev/v1alpha2");
        nsObject.put("kind", "Namespace");
        nsScopeObject.put("scopeRef", nsObject);
        scopes.add(nsScopeObject);

        configObject.put("revisionName", "K8S_MICROSERVICE|" + metaDO.getMicroServiceId() + "|_");

        /** - name: service.trait.abm.io
         *    runtime: post
         *    spec:
         *      ports:
         *      - protocol: TCP
         *        port: 80
         *        targetPort: 5000
         */
        JSONObject svcSpec = new JSONObject();
        JSONArray svcSpecPorts = new JSONArray();
        JSONObject portObject = new JSONObject();

        List<String> ports = new ArrayList<>(8);
        if (StringUtils.isNotBlank(launchObject.getServicePorts())) {
            ports = Arrays.stream(launchObject.getServicePorts().split(",")).collect(Collectors.toList());
        } else if (!Objects.isNull(launchObject.getServicePort())) {
            ports.add(launchObject.getServicePort().toString());
        } else {
            ports.add("7001");
        }
        for (String port : ports) {
            portObject.put("protocol", "TCP");
            if (port.split(":").length > 1) {
                portObject.put("port", port.split(":")[0]);
                portObject.put("targetPort", Long.valueOf(port.split(":")[1]));
            } else {
                portObject.put("port", 80);
                portObject.put("targetPort", Long.valueOf(port));
            }
            svcSpecPorts.add(portObject);
        }

        if (launchObject.getServiceLabels() != null) {
            svcSpec.put("labels", launchObject.getServiceLabels());
        }
        svcSpec.put("ports", svcSpecPorts);

        JSONObject svcTrait = new JSONObject();
        svcTrait.put("name", "service.trait.abm.io");
        svcTrait.put("runtime", "post");
        svcTrait.put("spec", svcSpec);
        traits.add(svcTrait);

        if (StringUtils.isNotBlank(launchObject.getGatewayRoute())) {

            /** - name: gateway.trait.abm.io
             *    runtime: post
             *    spec:
             *      path: /aiops/aisp/**
             *      servicePort: 80
             *      serviceName: '{{ Global.STAGE_ID }}-aiops-aisp.sreworks-aiops'
             */

            JSONObject gatewayTrait = new JSONObject();
            JSONObject gatewaySpec = new JSONObject();
            String gatewayRoute = launchObject.getGatewayRoute();
            if (!launchObject.getGatewayRoute().startsWith("/")) {
                gatewayRoute = "/" + gatewayRoute;
            }
            if (!launchObject.getGatewayRoute().endsWith("*")) {
                gatewayRoute = gatewayRoute + "/**";
            }
            gatewaySpec.put("path", gatewayRoute);
            gatewaySpec.put("serviceName", "{{ Global.STAGE_ID }}-" + metaDO.getAppId() + "-" + metaDO.getMicroServiceId() + ".{{ Global.NAMESPACE_ID }}");
            gatewayTrait.put("name", "gateway.trait.abm.io");
            gatewayTrait.put("runtime", "post");
            gatewayTrait.put("spec", gatewaySpec);
            if (launchObject.getGatewayRouteOrder() != null) {
                gatewaySpec.put("order", launchObject.getGatewayRouteOrder());
            }
            if (launchObject.getGatewayAuthEnabled() != null) {
                gatewaySpec.put("authEnabled", launchObject.getGatewayAuthEnabled());
            }

            traits.add(gatewayTrait);
        }

        if (launchObject.getReplicas() != null) {

            /**
             *         - name: REPLICAS
             *           value: 2
             *           toFieldPaths:
             *             - spec.replicas
             */
            JSONObject replicaValueObject = new JSONObject();
            JSONArray toFieldPaths = new JSONArray();
            toFieldPaths.add("spec.replicas");
            replicaValueObject.put("name", "REPLICAS");
            replicaValueObject.put("value", launchObject.getReplicas());
            replicaValueObject.put("toFieldPaths", toFieldPaths);
            parameterValues.add(replicaValueObject);
        }

        if (StringUtils.isNotBlank(launchObject.getTimezone())) {
            /**
             *         - name: timezoneSync.trait.abm.io
             *           runtime: pre
             *           spec:
             *             timezone: Asia/Shanghai
             */

            JSONObject timezoneTrait = new JSONObject();
            JSONObject timezoneSpec = new JSONObject();

            timezoneSpec.put("timezone", launchObject.getTimezone());
            timezoneTrait.put("name", "timezoneSync.trait.abm.io");
            timezoneTrait.put("runtime", "pre");
            timezoneTrait.put("spec", timezoneSpec);

            traits.add(timezoneTrait);
        }

        if (launchObject.getPodLabels() != null) {
            /**
             * name: podPatch.trait.abm.io
             * runtime: pre
             * spec:
             *     metadata:
             *       labels:
             *         a: b
             *       annotations:
             *         c: d
             */
            JSONObject podPatchTrait = new JSONObject();
            JSONObject podPatchSpec = new JSONObject();
            JSONObject podMetadata = new JSONObject();

            podPatchTrait.put("name", "podPatch.trait.abm.io");
            podPatchTrait.put("runtime", "pre");
            if (launchObject.getPodLabels() != null) {
                podMetadata.put("labels", launchObject.getPodLabels());
            }
            podPatchSpec.put("metadata", podMetadata);
            podPatchTrait.put("spec", podPatchSpec);
            traits.add(podPatchTrait);
        }

        JSONObject traitEnvMapList = new JSONObject();
        traitEnvMapList.put("APP_INSTANCE_ID", "{{ spec.labels[\"labels.appmanager.oam.dev/appInstanceId\"] }}");
        JSONArray traitEnvList = new JSONArray();

        for (String env : metaDO.getEnvKeyList()) {
            if (StringUtils.isBlank(env)) {
                continue;
            }
            String[] kv = env.split("=");
            String key = kv[0];
            if (traitEnvMapList.containsKey(key)) {
                traitEnvList.add(key);
            }
            if (kv.length > 1) {
                JSONObject valueObject = new JSONObject();
                valueObject.put("name", "Global." + key);
                valueObject.put("value", kv[1]);
                parameterValues.add(valueObject);
            }
        }

        if (traitEnvList.size() > 0) {
            JSONObject envTrait = new JSONObject();
            JSONArray dataOutputs = new JSONArray();
            envTrait.put("dataInputs", new JSONArray());
            envTrait.put("name", "systemEnv.trait.abm.io");
            envTrait.put("runtime", "pre");
            envTrait.put("spec", new JSONObject());
            for (int i = 0; i < traitEnvList.size(); i++) {
                String envKey = traitEnvList.getString(i);
                String envMapValue = traitEnvMapList.getString(envKey);
                JSONObject dataOutputObject = new JSONObject();
                dataOutputObject.put("fieldPath", envMapValue);
                dataOutputObject.put("name", "Global." + envKey);
                dataOutputs.add(dataOutputObject);
            }
            envTrait.put("dataOutputs", dataOutputs);
            traits.add(envTrait);
        }

        String systemTypeId = new DeployConfigTypeId(ComponentTypeEnum.RESOURCE_ADDON, "system-env@system-env").toString();

        List<DeployConfigDO> configs = deployConfigService.list(
                DeployConfigQueryCondition.builder()
                        .appId(metaDO.getAppId())
                        .typeId(systemTypeId)
                        .envId(DeployConfigEnvId.namespaceStageStr(metaDO.getNamespaceId(), metaDO.getStageId()))
                        .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                        .enabled(true)
                        .build()
        );

        // 如果存在system-env则直接进行依赖
        // todo: 判断自身的变量在system-env中有才进行依赖
        if (configs.size() > 0) {
            JSONArray dependencies = new JSONArray();
            JSONObject componentSystem = new JSONObject();
            componentSystem.put("component", "RESOURCE_ADDON|system-env@system-env");
            dependencies.add(componentSystem);
            configObject.put("dependencies", dependencies);
        }

        configObject.put("parameterValues", parameterValues);
        configObject.put("traits", traits);
        configObject.put("scopes", scopes);

        Yaml yaml = SchemaUtil.createYaml(JSONObject.class);
        String typeId = new DeployConfigTypeId(ComponentTypeEnum.K8S_MICROSERVICE, metaDO.getMicroServiceId()).toString();
        deployConfigService.update(DeployConfigUpdateReq.builder()
                .apiVersion(DefaultConstant.API_VERSION_V1_ALPHA2)
                .appId(metaDO.getAppId())
                .typeId(typeId)
                .envId(DeployConfigEnvId.namespaceStageStr(metaDO.getNamespaceId(), metaDO.getStageId()))
                .inherit(false)
                .config(yaml.dumpAsMap(configObject))
                .build());
    }

    /**
     * 更新指定的微应用元信息
     *
     * @param record 微应用元信息记录
     */
    @Override
    public int update(K8sMicroServiceMetaDO record, K8sMicroserviceMetaQueryCondition condition) {
        int res = k8sMicroServiceMetaRepository.updateByCondition(record, condition);
        this.refreshDeployConfig(record);
        return res;
    }

    /**
     * 创建指定的微应用元信息
     *
     * @param record 微应用元信息记录
     */
    @Override
    public int create(K8sMicroServiceMetaDO record) {
        int res = k8sMicroServiceMetaRepository.insert(record);
        this.refreshDeployConfig(record);
        return res;
    }

    /**
     * 根据主键 ID 删除微应用元信息
     *
     * @param id 微应用元信息主键 ID
     */
    @Override
    public int delete(Long id) {
        return k8sMicroServiceMetaRepository.deleteByPrimaryKey(id);
    }

    @Override
    public int delete(K8sMicroserviceMetaQueryCondition condition) {
        return k8sMicroServiceMetaRepository.deleteByCondition(condition);
    }
}
