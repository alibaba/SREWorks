package com.alibaba.tesla.appmanager.plugin.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.PluginProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.*;
import com.alibaba.tesla.appmanager.plugin.util.PluginNameGenerator;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
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
    public TeslaBaseResult list(
            @ParameterObject @ModelAttribute PluginQueryReq request, OAuth2Authentication auth) {
        return buildSucceedResult(pluginProvider.list(request));
    }

    @Operation(summary = "查询单个插件")
    @GetMapping("{pluginNamePrefix}/{pluginNameSuffix}/{pluginVersion}")
    public TeslaBaseResult getPluginInfo(
            @PathVariable("pluginNamePrefix") String pluginNamePrefix,
            @PathVariable("pluginNameSuffix") String pluginNameSuffix,
            @PathVariable("pluginVersion") String pluginVersion,
            OAuth2Authentication auth) throws IOException {
        String pluginName = PluginNameGenerator.generate(pluginNamePrefix, pluginNameSuffix);
        return buildSucceedResult(pluginProvider.get(PluginGetReq.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .build()));
    }

    @Operation(summary = "上传插件")
    @PostMapping
    public TeslaBaseResult upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "override", defaultValue = "true") Boolean override,
            @RequestParam(value = "enable", defaultValue = "false") Boolean enable,
            OAuth2Authentication auth) throws IOException {
        return buildSucceedResult(pluginProvider.upload(file, PluginUploadReq.builder()
                .enable(enable)
                .overwrite(override)
                .build()));
    }

    @Operation(summary = "操作插件")
    @PutMapping("{pluginNamePrefix}/{pluginNameSuffix}/{pluginVersion}/operate")
    public TeslaBaseResult operate(
            @PathVariable("pluginNamePrefix") String pluginNamePrefix,
            @PathVariable("pluginNameSuffix") String pluginNameSuffix,
            @PathVariable("pluginVersion") String pluginVersion,
            @RequestBody PluginOperateReq request,
            OAuth2Authentication auth) throws IOException {
        String pluginName = PluginNameGenerator.generate(pluginNamePrefix, pluginNameSuffix);
        if ("enable".equals(request.getOperation())) {
            PluginDefinitionDTO enablePlugin = pluginProvider.enable(PluginEnableReq.builder()
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .build());

            /**
             * disable all enable plugins with the same pluginName
             */
            if (request.getDisableOthers()) {
                Pagination<PluginDefinitionDTO> pluginList = pluginProvider.list(
                        PluginQueryReq.builder()
                                .pluginName(pluginName)
                                .pluginRegistered(true)
                                .build()
                );

                for (PluginDefinitionDTO plugin : pluginList.getItems()) {
                    if (plugin.getPluginName().equals(pluginName) && plugin.getPluginVersion().equals(pluginVersion)) {
                        continue;
                    }
                    pluginProvider.disable(PluginDisableReq.builder()
                            .pluginName(plugin.getPluginName())
                            .pluginVersion(plugin.getPluginVersion())
                            .ignoreGroovyFiles(true)
                            .build());
                }
            }

            return buildSucceedResult(enablePlugin);
        } else if ("disable".equals(request.getOperation())) {
            return buildSucceedResult(pluginProvider.disable(PluginDisableReq.builder()
                    .pluginName(pluginName)
                    .pluginVersion(pluginVersion)
                    .build()));
        } else {
            return buildClientErrorResult("invalid plugin operation");
        }
    }

    @Operation(summary = "获取插件前端资源")
    @GetMapping("{pluginNamePrefix}/{pluginNameSuffix}/{pluginVersion}/frontend/{name}")
    public TeslaBaseResult getPluginFrontend(
            @PathVariable("pluginNamePrefix") String pluginNamePrefix,
            @PathVariable("pluginNameSuffix") String pluginNameSuffix,
            @PathVariable("pluginVersion") String pluginVersion,
            @PathVariable("name") String name,
            OAuth2Authentication auth) throws IOException {
        String pluginName = PluginNameGenerator.generate(pluginNamePrefix, pluginNameSuffix);
        return buildSucceedResult(pluginProvider.getFrontend(PluginFrontendGetReq.builder()
                .pluginName(pluginName)
                .pluginVersion(pluginVersion)
                .name(name)
                .build()));
    }


    @Operation(summary = "获取多个插件前端资源")
    @GetMapping("frontend/resources")
    public TeslaBaseResult getPluginFrontendResources(
            @ParameterObject @ModelAttribute PluginFrontendResourcesReq request,
            OAuth2Authentication auth) throws IOException {
        if(request.getPlugins() != null){
            String[] plugins = request.getPlugins().split(",");
            JSONObject pluginFrontends = new JSONObject();
            for (int i = 0; i < plugins.length; i++) {
                pluginFrontends.put(
                    plugins[i],
                    pluginProvider.getFrontend(
                        PluginFrontendGetReq.builder()
                        .pluginName(plugins[i])
                        .pluginVersion("current")
                        .name(request.getName())
                        .build()
                    )
                );
            }
            return buildSucceedResult(pluginFrontends);
        }else{
            return buildClientErrorResult("no plugin");
        }
    }

}