package com.alibaba.tesla.appmanager.plugin.controller;

import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Plugin 管理
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/plugins")
@RestController
public class PluginController extends BaseController {



    /**
     * @api {get} /plugins 获取已安装的插件列表
     * @apiName GetPluginList
     * @apiGroup 插件关联微服务 API
     */
    @GetMapping
    public TeslaBaseResult list(@ModelAttribute PluginQueryReq request) {
        return buildSucceedResult(null);
    }

    /**
     * @api {post} /plugins/:pluginName 新增插件
     * @apiName PostPlugin
     * @apiGroup 插件关联微服务 API
     * @apiParam (Path Parameters) {String} pluginName 应用 ID
     * @apiParam (JSON Body) {String} microServiceId 微服务标识 ID
     * @apiParam (JSON Body) {String} name 微服务名称
     * @apiParam (JSON Body) {String} description 描述信息
     * @apiParam (JSON Body) {Object[]} containerObjectList 容器对象列表
     * @apiParam (JSON Body) {Object[]} envList 环境变量列表
     * @apiParam (JSON Body) {String="K8S_MICROSERVICE","K8S_JOB"} componentType 组件类型
     */
    @PostMapping
    public TeslaBaseResult create(
            @PathVariable String pluginName,
            @RequestBody K8sMicroServiceMetaUpdateReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp) {

        return buildSucceedResult(null);

    }

}