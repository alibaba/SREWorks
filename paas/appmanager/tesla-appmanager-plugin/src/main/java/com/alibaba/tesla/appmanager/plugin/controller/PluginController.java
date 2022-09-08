package com.alibaba.tesla.appmanager.plugin.controller;

import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginDisableReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginEnableReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginOperateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Plugin 管理
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "插件 API")
@RequestMapping("/plugins")
@RestController
public class PluginController extends AppManagerBaseController {

    @Autowired
    private PluginProvider pluginProvider;

    @Operation(summary = "查询插件列表")
    @GetMapping
    public TeslaBaseResult list(@ModelAttribute PluginQueryReq request, OAuth2Authentication auth) {
        return buildSucceedResult(pluginProvider.list(request));
    }

    @Operation(summary = "上传插件")
    @PostMapping
    public TeslaBaseResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "override", defaultValue = "true") Boolean override,
            @RequestParam(value = "enable", defaultValue = "false") Boolean enable,
            OAuth2Authentication auth) throws IOException {
        return buildSucceedResult(pluginProvider.upload(file, override, enable));
    }

    @Operation(summary = "操作插件")
    @PutMapping("{pluginName}/{pluginVersion}/operate")
    public TeslaBaseResult operate(
            @PathVariable("pluginName") String pluginName,
            @PathVariable("pluginVersion") String pluginVersion,
            @RequestBody PluginOperateReq request,
            OAuth2Authentication auth) throws IOException {
        if ("enable".equals(request.getOperation())) {
            return buildSucceedResult(pluginProvider.enable(PluginEnableReq.builder()
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .build()));
        } else if ("disable".equals(request.getOperation())) {
            return buildSucceedResult(pluginProvider.disable(PluginDisableReq.builder()
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .build()));
        } else {
            return buildClientErrorResult("invalid plugin operation");
        }
    }
}