package com.alibaba.tesla.appmanager.server.dynamicscript.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum;
import com.alibaba.tesla.appmanager.domain.req.rtcomponentinstance.RtComponentInstanceGetStatusReq;
import com.alibaba.tesla.appmanager.domain.res.rtcomponentinstance.RtComponentInstanceGetStatusRes;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandler;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;

/**
 * 组件自定义状态获取 Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface ComponentCustomStatusHandler extends GroovyHandler {

    /**
     * 由用户自行实现组件的自定义状态获取 Handler (默认实现直接返回 RUNNING 且不携带任何信息)
     *
     * @param request 状态获取请求
     * @param client  Kubernetes Client
     * @param options 配置信息字典
     * @return 实际状态
     */
    default RtComponentInstanceGetStatusRes getStatus(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options) {
        return RtComponentInstanceGetStatusRes.builder()
                .status(ComponentInstanceStatusEnum.RUNNING.toString())
                .conditions(new JSONArray())
                .build();
    }
}
