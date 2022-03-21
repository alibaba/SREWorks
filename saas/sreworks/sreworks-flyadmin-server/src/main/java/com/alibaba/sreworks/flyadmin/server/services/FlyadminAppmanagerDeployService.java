package com.alibaba.sreworks.flyadmin.server.services;

import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.Requests;
import com.alibaba.sreworks.common.util.UuidUtil;
import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DO.AppComponentInstance;
import com.alibaba.sreworks.domain.DO.AppInstance;
import com.alibaba.sreworks.domain.DO.ClusterResource;
import com.alibaba.sreworks.domain.DTO.AppComponentInstanceDetail;
import com.alibaba.sreworks.domain.DTO.Config;
import com.alibaba.sreworks.domain.DTO.Port;
import com.alibaba.sreworks.domain.DTO.Volume;
import com.alibaba.sreworks.domain.repository.AppComponentRepository;
import com.alibaba.sreworks.domain.repository.AppPackageRepository;
import com.alibaba.sreworks.domain.repository.ClusterResourceRepository;
import com.alibaba.tesla.web.constant.HttpHeaderNames;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import static com.alibaba.sreworks.domain.utils.AppUtil.appmanagerId;

/**
 * @author jinghua.yjh
 */
@Slf4j
@Service
public class FlyadminAppmanagerDeployService {

    @Autowired
    AppComponentRepository appComponentRepository;

    @Autowired
    ClusterResourceRepository clusterResourceRepository;

    @Autowired
    AppPackageRepository appPackageRepository;

    @Value("${metric.image}")
    private String metricImage;

    public List<JSONObject> list(Long appId, String user) throws IOException, ApiException {
        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments")
            .params("appId", appmanagerId(appId), "page", 1, "pageSize", 10000)
            .headers(HttpHeaderNames.X_EMPL_ID, user)
            .get().isSuccessful()
            .getJSONObject()
            .getJSONObject("data").getJSONArray("items").toJavaList(JSONObject.class);
    }

    public JSONObject get(String deployId, String user) throws IOException, ApiException {
        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments/" + deployId)
            .headers(HttpHeaderNames.X_EMPL_ID, user)
            .get().isSuccessful()
            .getJSONObject()
            .getJSONObject("data");
    }

    public String start(AppInstance appInstance, List<AppComponentInstance> appComponentInstanceList)
        throws IOException, ApiException {
        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments/launch")
            .postJson(getAc(appInstance, appComponentInstanceList))
            .headers(HttpHeaderNames.X_EMPL_ID, appInstance.getLastModifier())
            .post().isSuccessful()
            .getJSONObject()
            .getJSONObject("data").getString("deployAppId");
    }

    private JSONObject getAcMeta(AppInstance appInstance) {
        return JsonUtil.map(
            "apiVersion", "core.oam.dev/v1alpha2",
            "kind", "ApplicationConfiguration",
            "metadata", JsonUtil.map(
                "name", appmanagerId(appInstance.getAppId()),
                "annotations", JsonUtil.map(
                    "appId", appmanagerId(appInstance.getAppId()),
                    "appPackageId", appPackageRepository.findFirstById(appInstance.getAppPackageId()).getAppPackageId(),
                    //"clusterId", "master",
                    "clusterId", appInstance.getClusterId() + "id",
                    "namespaceId", appInstance.namespace(),
                    "stageId", appInstance.getStageId()
                )
            ),
            "spec", JsonUtil.map(
                "components", new JSONArray()
            )
        );
    }

    private void patchAcSpecComponent(
        JSONArray components, AppInstance appInstance, AppComponentInstance appComponentInstance) {
        components.add(JsonUtil.map(
            "dataInputs", new JSONArray(),
            "dataOutputs", new JSONArray(),
            "dependencies", new JSONArray(),
            "parameterValues", new JSONArray(),
            "revisionName", "K8S_MICROSERVICE|" + appComponentInstance.getName() + "|_",
            "scopes", JsonUtil.list(
                JsonUtil.map(
                    "scopeRef", JsonUtil.map(
                        "apiVersion", "flyadmin.alibaba.com/v1alpha1",
                        "kind", "Namespace",
                        "name", appInstance.namespace()
                    )
                ),
                JsonUtil.map(
                    "scopeRef", JsonUtil.map(
                        "apiVersion", "flyadmin.alibaba.com/v1alpha1",
                        "kind", "Cluster",
                        //"name", "master"
                        "name", appInstance.getClusterId() + "id"
                    )
                ),
                JsonUtil.map(
                    "scopeRef", JsonUtil.map(
                        "apiVersion", "flyadmin.alibaba.com/v1alpha1",
                        "kind", "Stage",
                        "name", appInstance.getStageId()
                    )
                )
            ),
            "traits", new JSONArray()
        ));
    }

    private JSONObject getServiceTrait(AppComponentInstanceDetail appComponentInstanceDetail) {
        List<Port> ports = appComponentInstanceDetail.ports();
        ports.add(Port.builder().name("metric").value(10080L).build());
        return JsonUtil.map(
            "name", "service.trait.abm.io",
            "runtime", "post",
            "spec", JsonUtil.map(
                "labels", JsonUtil.map(
                    "sreworks", appComponentInstanceDetail.metricOn() ? "metric" : "metricOff"
                ),
                "ports", ports.stream().map(port -> JsonUtil.map(
                    "name", port.getName(),
                    "protocol", "TCP",
                    "port", port.getValue(),
                    "targetPort", port.getValue()
                )).collect(Collectors.toList())
            )
        );
    }

    private JSONObject getMetricContainer() {
        return JsonUtil.map(
            "name", "metric",
            "image", metricImage,
            "resources", JsonUtil.map(
                "limits", JsonUtil.map(
                    "cpu", "0",
                    "memory", "0G"
                ),
                "requests", JsonUtil.map(
                    "cpu", "0",
                    "memory", "0G"
                )
            )
        );
    }

    private JSONObject getIntegrateTraitContainer(AppComponentInstance appComponentInstance) {
        AppComponentInstanceDetail appComponentInstanceDetail = appComponentInstance.detail();
        return JsonUtil.map(
            "env", appComponentInstanceDetail.envs(),
            "resources", JsonUtil.map(
                "limits", JsonUtil.map(
                    "cpu", appComponentInstance.detail().getResource().getLimits().getCpu(),
                    "memory", appComponentInstance.detail().getResource().getLimits().getMemory()
                ),
                "requests", JsonUtil.map(
                    "cpu", appComponentInstance.detail().getResource().getRequests().getCpu(),
                    "memory", appComponentInstance.detail().getResource().getRequests().getMemory()
                )
            )
        );
    }

    private void patchAcSpecComponentTraits(JSONArray traits,
        AppComponentInstance appComponentInstance) {
        AppComponentInstanceDetail appComponentInstanceDetail = appComponentInstance.detail();
        traits.add(JsonUtil.map(
            "name", "integrate.trait.abm.io",
            "runtime", "pre",
            "spec", JsonUtil.map(
                "content", JsonUtil.map(
                    "replicas", appComponentInstanceDetail.getReplicas(),
                    "containers", JsonUtil.list(
                        getIntegrateTraitContainer(appComponentInstance),
                        getMetricContainer()
                    )
                ),
                "others", new JSONArray()
            )
        ));
        traits.add(getServiceTrait(appComponentInstanceDetail));
    }

    private void patchVolume(JSONObject integrateContent, JSONArray integrateOthers,
        AppInstance appInstance, AppComponentInstance appComponentInstance) {

        JSONObject container = integrateContent.getJSONArray("containers").getJSONObject(0);
        if (!integrateContent.containsKey("volumes")) {
            integrateContent.put("volumes", new JSONArray());
        }
        if (!container.containsKey("volumeMounts")) {
            container.put("volumeMounts", new JSONArray());
        }
        AppComponentInstanceDetail appComponentInstanceDetail = appComponentInstance.detail();
        List<Volume> volumes = appComponentInstanceDetail.volumes();
        for (Volume volume : volumes) {
            container.getJSONArray("volumeMounts").add(JsonUtil.map(
                "mountPath", volume.getPath(),
                "name", volume.getName()
            ));
            integrateContent.getJSONArray("volumes").add(JsonUtil.map(
                "name", volume.getName(),
                "persistentVolumeClaim", JsonUtil.map(
                    "claimName", volume.getName()
                )
            ));
            integrateOthers.add(JsonUtil.map(
                "apiVersion", "v1",
                "kind", "PersistentVolumeClaim",
                "metadata", JsonUtil.map(
                    "name", volume.getName(),
                    "namespace", appInstance.namespace()
                ),
                "spec", JsonUtil.map(
                    "accessModes", JsonUtil.list("ReadWriteMany"),
                    "resources", JsonUtil.map(
                        "requests", JsonUtil.map(
                            "storage", volume.getStorage()
                        )
                    ),
                    "storageClassName", volume.getStorageClassName()
                )
            ));
        }

    }

    private void patchConfig(JSONObject integrateContent, JSONArray integrateOthers,
        AppInstance appInstance, AppComponentInstance appComponentInstance) {
        JSONObject container = integrateContent.getJSONArray("containers").getJSONObject(0);
        if (!container.containsKey("volumeMounts")) {
            container.put("volumeMounts", new JSONArray());
        }
        if (!integrateContent.containsKey("volumes")) {
            integrateContent.put("volumes", new JSONArray());
        }
        AppComponentInstanceDetail appComponentInstanceDetail = appComponentInstance.detail();
        List<Config> configs = appComponentInstanceDetail.configs();
        for (Config config : configs) {
            String name = UuidUtil.shortUuid();
            container.getJSONArray("volumeMounts").add(JsonUtil.map(
                "mountPath", config.getParentPath(),
                "name", name
            ));
            integrateContent.getJSONArray("volumes").add(JsonUtil.map(
                "name", name,
                "configMap", JsonUtil.map(
                    "name", name
                )
            ));
            integrateOthers.add(JsonUtil.map(
                "apiVersion", "v1",
                "kind", "ConfigMap",
                "metadata", JsonUtil.map(
                    "name", name,
                    "namespace", appInstance.namespace()
                ),
                "data", JsonUtil.map(
                    config.getName(), config.getContent()
                )
            ));
        }

    }

    private void patchClusterResourceEnvConfig(JSONObject integrateContent, JSONArray integrateOthers,
        AppInstance appInstance) {
        String name = UuidUtil.shortUuid();
        JSONObject container = integrateContent.getJSONArray("containers").getJSONObject(0);
        if (!container.containsKey("envFrom")) {
            container.put("envFrom", new JSONArray());
        }
        List<Long> clusterResourceIdList = appInstance.detail().clusterResourceIdList();
        if (CollectionUtils.isEmpty(clusterResourceIdList)) {
            return;
        }
        container.getJSONArray("envFrom").add(JsonUtil.map(
            "configMapRef", JsonUtil.map(
                "name", name
            )
        ));
        JSONObject data = new JSONObject();
        for (Long clusterResourceId : clusterResourceIdList) {
            ClusterResource clusterResource = clusterResourceRepository.findFirstById(clusterResourceId);
            for (Entry<String, Object> entry : clusterResource.usageDetail().entrySet()) {
                data.put(
                    clusterResource.getName() + "_" + entry.getKey(),
                    entry.getValue()
                );
            }
        }

        integrateOthers.add(JsonUtil.map(
            "apiVersion", "v1",
            "kind", "ConfigMap",
            "metadata", JsonUtil.map(
                "name", name,
                "namespace", appInstance.namespace()
            ),
            "data", data
        ));
    }

    private String getAc(AppInstance appInstance, List<AppComponentInstance> appComponentInstanceList)
        throws JsonProcessingException {
        JSONObject acJsonObject = getAcMeta(appInstance);
        JSONArray components = acJsonObject.getJSONObject("spec").getJSONArray("components");
        for (AppComponentInstance appComponentInstance : appComponentInstanceList) {
            patchAcSpecComponent(components, appInstance, appComponentInstance);
            JSONArray traits = components.getJSONObject(components.size() - 1).getJSONArray("traits");
            patchAcSpecComponentTraits(traits, appComponentInstance);

            JSONObject integrateContent = traits.getJSONObject(0).getJSONObject("spec").getJSONObject("content");
            JSONArray integrateOthers = traits.getJSONObject(0).getJSONObject("spec").getJSONArray("others");
            patchConfig(integrateContent, integrateOthers, appInstance, appComponentInstance);
            patchVolume(integrateContent, integrateOthers, appInstance, appComponentInstance);
            patchClusterResourceEnvConfig(integrateContent, integrateOthers, appInstance);
        }
        log.info(YamlUtil.toYaml(acJsonObject));
        return YamlUtil.toYaml(acJsonObject);
    }

    public JSONObject logs(String id, String user) throws IOException, ApiException {
        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments/" + id + "/attributes")
            .headers(HttpHeaderNames.X_EMPL_ID, user)
            .get().isSuccessful()
            .getJSONObject()
            .getJSONObject("data");
    }

}
