package io.sreworks.appmarket.server.params;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

@Data
public class AppMarketEndpointUpsertParam {

    private String name;

    private JSONObject config;

}
