package io.sreworks.appdev.server.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.Requests;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.tesla.web.constant.HttpHeaderNames;
import io.kubernetes.client.openapi.ApiException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
public class AppmanagerDeployService {

    public List<JSONObject> list(String appId, String user) throws IOException, ApiException {
        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments")
                .params("appId", appId, "page", 1, "pageSize", 10000)
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .get().isSuccessful()
                .getJSONObject()
                .getJSONObject("data").getJSONArray("items").toJavaList(JSONObject.class);
    }

    public JSONObject listPagination(
            String appId, String user,
            String page, String pageSize,
            String stageIdWhiteList, String optionKey, String optionValue
    ) throws IOException, ApiException {
        JSONObject payload = new JSONObject();

        if(appId != null){
            payload.put("appId", appId);
        }
        if(stageIdWhiteList != null) {
            payload.put("stageIdWhiteList", stageIdWhiteList);
        }
        if(optionKey != null){
            payload.put("optionKey", optionKey);
        }
        if(optionValue != null){
            payload.put("optionValue", optionValue);

        }
        payload.put("page", page);
        payload.put("pageSize", pageSize);

        return new Requests(AppmanagerServiceUtil.getEndpoint() + "/deployments", true)
                .params(payload)
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .get().isSuccessful()
                .getJSONObject()
                .getJSONObject("data");
    }

}
