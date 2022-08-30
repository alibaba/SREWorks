package com.alibaba.tesla.appmanager.plugin.controller;

import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Plugin 管理
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/plugins")
@RestController
public class PluginController extends BaseController {


    @Autowired
    private PluginProvider pluginProvider;

    /**
     * @api {get} /plugins 获取已安装的插件列表
     * @apiName GetPluginList
     * @apiGroup 插件API
     */
    @GetMapping
    public TeslaBaseResult list(@ModelAttribute PluginQueryReq request) {
        return buildSucceedResult(null);
    }

    /**
     * @api {post} /plugins 新增插件
     * @apiName UploadPlugin
     * @apiGroup 插件API
     */
    @PostMapping
    public TeslaBaseResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        return buildSucceedResult(pluginProvider.upload(file, true));
    }
}