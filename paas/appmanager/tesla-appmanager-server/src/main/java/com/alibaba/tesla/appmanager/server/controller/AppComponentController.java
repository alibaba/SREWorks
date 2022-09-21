package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.container.BizAppContainer;
import com.alibaba.tesla.appmanager.domain.dto.AppComponentDTO;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentCreateReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentQueryReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentUpdateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Component Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "应用关联组件 API")
@RequestMapping("/apps/{appId}/components")
@RestController
public class AppComponentController extends AppManagerBaseController {

    @Autowired
    private AppComponentProvider appComponentProvider;

    @GetMapping
    @Operation(summary = "获取应用关联的组件列表")
    public TeslaBaseResult list(
            @PathVariable String appId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            @ModelAttribute AppComponentQueryReq req,
            OAuth2Authentication auth) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        return buildSucceedResult(appComponentProvider.list(AppComponentQueryReq.builder()
                .appId(appId)
                .namespaceId(container.getNamespaceId())
                .stageId(container.getStageId())
                .arch(req.getArch())
                .build(), getOperator(auth)));
    }

    @PostMapping
    @Operation(summary = "创建应用关联的指定组件")
    public TeslaBaseResult create(
            @PathVariable String appId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            @RequestBody AppComponentCreateReq request,
            OAuth2Authentication auth) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setAppId(appId);
        request.setNamespaceId(container.getNamespaceId());
        request.setStageId(container.getStageId());
        AppComponentDTO result = appComponentProvider.create(request, getOperator(auth));
        return buildSucceedResult(result);
    }

    @GetMapping("{appComponentId}")
    @Operation(summary = "获取应用关联的指定组件")
    public TeslaBaseResult get(
            @PathVariable String appId,
            @PathVariable Long appComponentId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        AppComponentDTO result = appComponentProvider.get(
                AppComponentQueryReq.builder().id(appComponentId).build(),
                getOperator(auth));
        if (!container.getNamespaceId().equals(result.getNamespaceId())
                || !container.getStageId().equals(result.getStageId())
                || !appId.equals(result.getAppId())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "mismatched appId/namespaceId/stageId");
        }
        return buildSucceedResult(result);
    }

    @PutMapping("{appComponentId}")
    @Operation(summary = "更新应用关联的指定组件")
    public TeslaBaseResult update(
            @PathVariable String appId,
            @PathVariable Long appComponentId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            @RequestBody AppComponentUpdateReq request,
            OAuth2Authentication auth) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setId(appComponentId);
        request.setNamespaceId(container.getNamespaceId());
        request.setStageId(container.getStageId());
        AppComponentDTO result = appComponentProvider.update(request, getOperator(auth));
        return buildSucceedResult(result);
    }

    @DeleteMapping("{appComponentId}")
    @Operation(summary = "删除应用关联的指定组件")
    public TeslaBaseResult delete(
            @PathVariable String appId,
            @PathVariable Long appComponentId,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        String operator = getOperator(auth);
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        AppComponentDTO record = appComponentProvider.get(AppComponentQueryReq.builder()
                        .id(appComponentId)
                        .build(), operator);
        if (record == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find specified app component record");
        }
        if (!record.getAppId().equals(appId)
                || !record.getNamespaceId().equals(container.getNamespaceId())
                || !record.getStageId().equals(container.getStageId())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "mismatched appId/namespaceId/stageId");
        }
        AppComponentDeleteReq request = AppComponentDeleteReq.builder()
                .id(appComponentId)
                .build();
        appComponentProvider.delete(request, operator);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
