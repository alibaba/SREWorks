package com.alibaba.sreworks.appdev.server.services;

import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.Requests;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.tesla.web.constant.HttpHeaderNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.alibaba.sreworks.domain.utils.AppUtil.appmanagerId;

@Slf4j
@Service
public class AppmanagerService {

    public void create(App app) throws Exception {
        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps")
                .postJson(
                        "appId", app.getName(),
                        "options", JsonUtil.map(
                                "name", app.getName(),
                                "source", "app",
                                "apiVersion", "v2"
                        )
                )
                .headers(HttpHeaderNames.X_EMPL_ID, app.getCreator())
                .post().isSuccessful();
    }

    public void delete(String appId, String user) throws Exception {
        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appId)
                .headers(HttpHeaderNames.X_EMPL_ID, user)
                .delete().isSuccessful();
    }

}
