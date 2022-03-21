package com.alibaba.sreworks.appdev.server.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.K8sUtil;
import com.alibaba.sreworks.domain.DO.AppComponent;
import com.alibaba.sreworks.domain.DO.AppComponentInstance;
import com.alibaba.sreworks.domain.DO.AppInstance;
import com.alibaba.sreworks.domain.DO.Cluster;
import com.alibaba.sreworks.domain.DTO.AppInstanceDetail;
import com.alibaba.sreworks.domain.repository.AppComponentInstanceRepository;
import com.alibaba.sreworks.domain.repository.AppComponentRepository;
import com.alibaba.sreworks.domain.repository.AppInstanceRepository;
import com.alibaba.sreworks.domain.repository.AppRepository;
import com.alibaba.sreworks.domain.repository.ClusterRepository;
import com.alibaba.sreworks.flyadmin.server.services.FlyadminAppmanagerDeployService;

import com.google.common.collect.ImmutableMap;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Namespace;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ResourceQuota;
import io.kubernetes.client.openapi.models.V1ResourceQuotaSpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppUpdateService extends AbstractAppDeployService {

    private void patchNamespace(AppInstance appInstance) throws IOException, ApiException {
        CoreV1Api api = api(appInstance);
        JSONArray patchArray = JsonUtil.list(JsonUtil.map(
            "op", "replace",
            "path", "/metadata/labels/appInstanceId",
            "value", appInstance.getId().toString()
        ));
        V1Patch patch = new V1Patch(JSONObject.toJSONString(patchArray));
        api.patchNamespace(appInstance.namespace(), patch, null, null, null, null);
    }

    public void replaceResourceQuota(AppInstance appInstance) throws IOException, ApiException {
        CoreV1Api api = api(appInstance);
        api.replaceNamespacedResourceQuota("mem-cpu", appInstance.namespace(), getResourceQuota(appInstance),
            null, null, null);
    }

    void flushAppComponentInstanceList(AppInstance appInstance, List<AppComponentInstance> appComponentInstanceList) {

        List<AppComponentInstance> nowAppComponentInstanceList = appComponentInstanceRepository
            .findAllByAppInstanceId(appInstance.getId());
        Map<String, AppComponentInstance> nowAppComponentInstanceMap = nowAppComponentInstanceList.stream()
            .collect(Collectors.toMap(AppComponentInstance::getName, x -> x));
        List<String> nameList = appComponentInstanceList.stream()
            .map(AppComponentInstance::getName).collect(Collectors.toList());

        for (AppComponentInstance appComponentInstance : appComponentInstanceList) {
            AppComponentInstance nowAppComponentInstance = nowAppComponentInstanceMap.get(
                appComponentInstance.getName());
            if (nowAppComponentInstance != null) {
                appComponentInstance.setId(nowAppComponentInstance.getId());
            }
        }
        appComponentInstanceRepository.saveAll(appComponentInstanceList);
        appComponentInstanceRepository.flush();
        for (AppComponentInstance nowAppComponentInstance : nowAppComponentInstanceList) {
            if (!nameList.contains(nowAppComponentInstance.getName())) {
                appComponentInstanceRepository.deleteById(nowAppComponentInstance.getId());
            }
        }
    }

    public void update(AppInstance appInstance, List<AppComponentInstance> appComponentInstanceList, String user)
        throws IOException, ApiException {
        patchNamespace(appInstance);
        replaceResourceQuota(appInstance);
        flushAppComponentInstanceList(appInstance, appComponentInstanceList);
        run(appInstance, appComponentInstanceList);
        appInstance.setLastModifier(user);
        appInstance.setGmtModified(System.currentTimeMillis() / 1000);
    }

}
