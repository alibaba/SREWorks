package com.alibaba.sreworks.flyadmin.server.services;

import com.alibaba.sreworks.common.util.AppmanagerServiceUtil;
import com.alibaba.sreworks.common.util.JsonUtil;
import com.alibaba.sreworks.common.util.Requests;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.tesla.web.constant.HttpHeaderNames;

import org.springframework.stereotype.Service;

import static com.alibaba.sreworks.domain.utils.AppUtil.appmanagerId;

/**
 * @author jinghua.yjh
 */
@Service
public class FlyadminAppmanagerAppService {

    public void create(App app) throws Exception {
        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps")
            .postJson(
                "appId", appmanagerId(app.getId()),
                "options", JsonUtil.map(
                    "name", app.getName(),
                    "source", "app"
                )
            )
            .headers(HttpHeaderNames.X_EMPL_ID, app.getCreator())
            .post().isSuccessful();
    }

    public void delete(Long appId, String user) throws Exception {
        new Requests(AppmanagerServiceUtil.getEndpoint() + "/apps/" + appmanagerId(appId))
            .headers(HttpHeaderNames.X_EMPL_ID, user)
            .delete().isSuccessful();
    }

}
