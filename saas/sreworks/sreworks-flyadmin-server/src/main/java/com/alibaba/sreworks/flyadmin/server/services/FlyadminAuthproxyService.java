package com.alibaba.sreworks.flyadmin.server.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.K8sUtil;
import com.alibaba.sreworks.common.util.Requests;
import com.alibaba.tesla.web.constant.HttpHeaderNames;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FlyadminAuthproxyService {

    public static String authProxyEndpoint;

    @SuppressWarnings("ConstantConditions")
    public static String getAuthProxyEndpoint() throws IOException, ApiException {
        if (authProxyEndpoint == null) {
            List<V1Service> serviceList = K8sUtil.listServiceByFieldSelector("");
            for (V1Service service : serviceList) {
                try {
                    if (service.getMetadata().getName().contains("authproxy")) {
                        authProxyEndpoint = K8sUtil.getServiceEndpoint(service).get(0);
                        break;
                    }
                } catch (Exception e) {
                    log.warn("", e);
                }
            }

        }
        return authProxyEndpoint;
    }

    public List<JSONObject> userList(String user) throws IOException, ApiException {
        String url = getAuthProxyEndpoint() + "/auth/user/list?userName=";
        return new Requests(url)
            .headers(HttpHeaderNames.X_EMPL_ID, user)
            .get().isSuccessful()
            .getJSONObject().getJSONArray("data").toJavaList(JSONObject.class);
    }

    public Map<String, String> userEmpIdNameMap(String user) throws IOException, ApiException {
        return userList(user).stream().collect(Collectors.toMap(
            jsonObject -> jsonObject.getString("empId"),
            jsonObject -> jsonObject.getString("nickName"),
            (v1, v2) -> v1
        ));
    }

}
