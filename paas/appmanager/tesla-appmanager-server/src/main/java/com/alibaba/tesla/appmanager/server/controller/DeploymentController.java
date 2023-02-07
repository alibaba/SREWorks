package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.DeployAppProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.DeployAppAttrDTO;
import com.alibaba.tesla.appmanager.domain.dto.DeployAppDTO;
import com.alibaba.tesla.appmanager.domain.dto.DeployComponentAttrDTO;
import com.alibaba.tesla.appmanager.domain.req.deploy.*;
import com.alibaba.tesla.appmanager.domain.res.deploy.DeployAppPackageLaunchRes;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;

/**
 * 部署单管理
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "部署单 API")
@RequestMapping("/deployments")
@RestController
public class DeploymentController extends AppManagerBaseController {

    @Autowired
    private DeployAppProvider deployAppProvider;

    @Operation(summary = "发起部署")
    @PostMapping(value = "/launch")
    @ResponseBody
    public TeslaBaseResult launch(
            @ParameterObject @Valid @ModelAttribute DeployAppLaunchReq request,
            @RequestBody String body, OAuth2Authentication auth
    ) {
        request.setConfiguration(body);
        try {
            DeployAppPackageLaunchRes response = deployAppProvider.launch(request, getOperator(auth));
            return buildSucceedResult(response);
        } catch (Exception e) {
            log.error("cannot launch deployments|exception={}|yaml={}", ExceptionUtils.getStackTrace(e), body);
            return buildExceptionResult(e);
        }
    }

    @Operation(summary = "发起快速部署")
    @PostMapping(value = "/fast-launch")
    @ResponseBody
    public TeslaBaseResult fastLaunch(@RequestBody FastDeployAppLaunchReq request, OAuth2Authentication auth) {
        try {
            DeployAppPackageLaunchRes response = deployAppProvider.fastLaunch(request, getOperator(auth));
            return buildSucceedResult(response);
        } catch (Exception e) {
            log.error("cannot launch deployments|exception={}|request={}",
                    ExceptionUtils.getStackTrace(e), JSONObject.toJSONString(request));
            return buildExceptionResult(e);
        }
    }

    @Operation(summary = "查询部署单列表")
    @GetMapping
    @ResponseBody
    public TeslaBaseResult list(
            @ParameterObject @ModelAttribute DeployAppListReq request,
            OAuth2Authentication auth
    ) throws Exception {
        Pagination<DeployAppDTO> response = deployAppProvider.list(request, getOperator(auth));
        return buildSucceedResult(response);
    }

    @Operation(summary = "重新发起指定部署单")
    @PostMapping("{deployAppId}/replay")
    @ResponseBody
    public TeslaBaseResult replay(
            @PathVariable("deployAppId") Long deployAppId, OAuth2Authentication auth
    ) throws Exception {
        DeployAppReplayReq request = DeployAppReplayReq.builder()
                .deployAppId(deployAppId)
                .build();
        try {
            DeployAppPackageLaunchRes response = deployAppProvider.replay(request, getOperator(auth));
            return buildSucceedResult(response);
        } catch (Exception e) {
            log.error("cannot launch deployments|exception={}|originDeployId={}",
                    ExceptionUtils.getStackTrace(e), deployAppId);
            return buildExceptionResult(e);
        }
    }

    @Operation(summary = "查询部署单详情")
    @GetMapping("{deployAppId}")
    @ResponseBody
    public TeslaBaseResult get(
            @PathVariable("deployAppId") Long deployAppId, OAuth2Authentication auth
    ) throws Exception {
        if (deployAppId == 0) {
            return buildSucceedResult(new DeployAppDTO());
        }
        
        DeployAppGetReq request = DeployAppGetReq.builder()
                .deployAppId(deployAppId)
                .build();
        DeployAppDTO response = deployAppProvider.get(request, getOperator(auth));
        return buildSucceedResult(response);
    }

    @Operation(summary = "查询部署单属性")
    @GetMapping("{deployAppId}/attributes")
    @ResponseBody
    public TeslaBaseResult getAttributes(
            @PathVariable("deployAppId") Long deployAppId, OAuth2Authentication auth
    ) throws Exception {
        DeployAppGetAttrReq request = DeployAppGetAttrReq.builder()
                .deployAppId(deployAppId)
                .build();
        DeployAppAttrDTO response = deployAppProvider.getAttr(request, getOperator(auth));
        return buildSucceedResult(response);
    }

    @Operation(summary = "查询部署单下指定组件属性")
    @GetMapping("{deployAppId}/components/{deployComponentId}/attributes")
    @ResponseBody
    public TeslaBaseResult getComponentAttributes(
            @PathVariable("deployAppId") Long deployAppId,
            @PathVariable("deployComponentId") Long deployComponentId,
            OAuth2Authentication auth
    ) {
        DeployAppGetComponentAttrReq request = DeployAppGetComponentAttrReq.builder()
                .deployComponentId(deployComponentId)
                .build();
        DeployComponentAttrDTO response = deployAppProvider.getComponentAttr(request, getOperator(auth));
        return buildSucceedResult(response);
    }

    @Operation(summary = "重试部署单")
    @PostMapping("{deployAppId}/retry")
    @ResponseBody
    public TeslaBaseResult retry(
            @PathVariable("deployAppId") Long deployAppId, OAuth2Authentication auth) {
        DeployAppRetryReq request = DeployAppRetryReq.builder()
                .deployAppId(deployAppId)
                .build();
        deployAppProvider.retry(request, getOperator(auth));
        return buildSucceedResult(new HashMap<String, String>());
    }

    @Operation(summary = "终止部署单")
    @PostMapping("{deployAppId}/terminate")
    @ResponseBody
    public TeslaBaseResult terminate(
            @PathVariable("deployAppId") Long deployAppId, OAuth2Authentication auth) {
        DeployAppTerminateReq request = DeployAppTerminateReq.builder()
                .deployAppId(deployAppId)
                .build();
        deployAppProvider.terminate(request, getOperator(auth));
        return buildSucceedResult(new HashMap<String, String>());
    }
}
