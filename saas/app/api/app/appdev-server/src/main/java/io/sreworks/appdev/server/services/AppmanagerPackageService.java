package io.sreworks.appdev.server.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.tesla.web.constant.HttpHeaderNames;
import io.kubernetes.client.openapi.ApiException;
import io.sreworks.common.util.Requests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;


@Slf4j
@Service
public class AppmanagerPackageService {

    public List<JSONObject> list(Long appId, String user) throws IOException, ApiException {
        return new com.alibaba.sreworks.common.util.Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId + "/app-packages")
                .params("page", 1, "pageSize", 10000).headers(HttpHeaderNames.X_EMPL_ID, user)
                .get().isSuccessful()
                .getJSONObject()
                .getJSONObject("data").getJSONArray("items").toJavaList(JSONObject.class);
    }

    public Long count(String appId, String user) throws IOException, ApiException {
        Long cnt = Long.valueOf(0);
        List<JSONObject> packages = new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId + "/app-packages")
                .params("page", 1, "pageSize", 10000).headers(HttpHeaderNames.X_EMPL_ID, user)
                .get().isSuccessful()
                .getJSONObject()
                .getJSONObject("data").getJSONArray("items").toJavaList(JSONObject.class);
        for(JSONObject p: packages){
            cnt += 1;
        }
        return cnt;
    }

}
