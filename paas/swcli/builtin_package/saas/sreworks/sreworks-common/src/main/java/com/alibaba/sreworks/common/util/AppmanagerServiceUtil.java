package com.alibaba.sreworks.common.util;

import java.io.IOException;
import java.util.List;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Service;
import lombok.extern.slf4j.Slf4j;

/**
 * @author jinghua.yjh
 */
@Slf4j
public class AppmanagerServiceUtil {

    public static String endpoint;

    public static String getEndpoint() throws IOException, ApiException {
        if (endpoint == null) {
            endpoint = K8sUtil.getServiceEndpoint(
                K8sUtil.listServiceByFieldSelector("metadata.name=dev-sreworks-appmanager").get(0)
                //K8sUtil.listServiceByFieldSelector("metadata.name=appmanager").get(0)
            ).get(0);
        }
        return endpoint;
    }

}
