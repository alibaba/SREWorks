package io.sreworks.appdev.server.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.tesla.web.constant.HttpHeaderNames;
import io.sreworks.common.util.Requests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AppmanagerComponentService {

    public JSONArray list(String appId, String user) throws Exception {

        JSONObject componentsConfigs = new JSONObject();
        List<JSONObject> deployConfigs = new Requests(AppmanagerServiceUtil.getEndpoint() + "/application-configurations/types/components?appId=" + appId + "&typeIdPrefix=Type:components")
                .get()
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .isSuccessful().getJSONObject().getJSONObject("data").getJSONArray("items").toJavaList(JSONObject.class);

        for(JSONObject deployConfig: deployConfigs){
            componentsConfigs.put(deployConfig.getString("typeId"), deployConfig);
        }

        log.info("GET " + AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId + "/components");
        List<JSONObject> components = new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId + "/components?withBlobs=true")
                .get()
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .isSuccessful().getJSONObject().getJSONArray("data").toJavaList(JSONObject.class);
        for(JSONObject component: components){
            component.put("deployConfig", componentsConfigs.getJSONObject(component.getString("typeId")));
        }
        return JSONArray.parseArray(JSON.toJSONString(components));
    }

}
