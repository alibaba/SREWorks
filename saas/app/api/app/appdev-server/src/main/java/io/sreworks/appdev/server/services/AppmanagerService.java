package io.sreworks.appdev.server.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import io.sreworks.common.util.Requests;

import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.tesla.web.constant.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.alibaba.sreworks.domain.utils.AppUtil.appmanagerId;

@Slf4j
@Service
public class AppmanagerService {

    public void create(App app) throws Exception {
        log.info("POST " + AppmanagerServiceUtil.getEndpoint() + "/apps");

        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps")
                .postJson(
              "appId", app.getName(),
                    "options", getOptions(app)
                )
                .headers(HttpHeaderNames.X_EMPL_ID, app.getCreator())
                .post().isSuccessful();
    }

    public void update(App app) throws Exception {
        log.info("PUT " + AppmanagerServiceUtil.getEndpoint() + "/apps/" + app.getName());

        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + app.getName())
                .postJson(
                        "appId", app.getName(),
                    "options", getOptions(app)
                )
                .headers(HttpHeaderNames.X_EMPL_ID, app.getCreator())
                .put().isSuccessful();
    }

    private JSONObject getOptions(App app){
        JSONObject options = JSONObject.parseObject(app.getDetail()).getJSONObject("options");
        if(options == null) {
            options = new JSONObject();
        }
        options.put("name", app.getName());
        options.put("source", "app");
        options.put("apiVersion", "v2");
        return options;
    }

    public void delete(String appId, String user) throws Exception {
        log.info("DELETE " + AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId);
        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId)
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .delete().isSuccessful();
    }

}
