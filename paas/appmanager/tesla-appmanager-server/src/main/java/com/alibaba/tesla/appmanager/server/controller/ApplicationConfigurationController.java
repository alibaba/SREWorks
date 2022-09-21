package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.DeployConfigProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.BizAppContainer;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.*;
import com.alibaba.tesla.appmanager.domain.res.apppackage.ApplicationConfigurationGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Application Configuration Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Tag(name = "部署配置 API")
@RequestMapping("/application-configurations")
@RestController
@Slf4j
public class ApplicationConfigurationController extends AppManagerBaseController {

    @Autowired
    private DeployConfigProvider deployConfigProvider;

    @Operation(summary = "更新全局部署信息")
    @PutMapping
    public TeslaBaseResult update(
            @RequestBody DeployConfigApplyTemplateReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        } else if (StringUtils.isEmpty(request.getEnvId())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty envId is not allowed");
        }

        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        return buildSucceedResult(deployConfigProvider.applyTemplate(request));
    }

    @Operation(summary = "查询全局部署信息详情")
    @GetMapping
    public TeslaBaseResult get(
            @ModelAttribute DeployConfigGenerateReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        request.setAppPackageId(0L);

        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        DeployConfigGenerateRes result = deployConfigProvider.generate(request);
        return buildSucceedResult(ApplicationConfigurationGenerateRes.builder()
                .yaml(SchemaUtil.toYamlMapStr(result.getSchema()))
                .build());
    }

    @Operation(summary = "更新指定类型的部署信息")
    @PutMapping("types/{type}")
    public TeslaBaseResult upsertByType(
            @RequestBody DeployConfigUpsertReq request,
            @PathVariable String type,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        DeployConfigTypeId typeIdObj = DeployConfigTypeId.valueOf(request.getTypeId());
        if (!typeIdObj.getType().equals(type)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "mismatched type " + type);
        }
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        DeployConfigDTO result = deployConfigProvider.upsert(request);
        return buildSucceedResult(result);
    }

    @Operation(summary = "查询指定类型的部署信息列表")
    @GetMapping("types/{type}")
    public TeslaBaseResult listByType(
            @ModelAttribute DeployConfigListReq request,
            @PathVariable String type,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        String typeId = request.getTypeId();
        if (StringUtils.isEmpty(typeId)) {
            typeId = request.getTypeIdPrefix();
        }
        DeployConfigTypeId typeIdObj = DeployConfigTypeId.valueOf(typeId);
        if (!typeIdObj.getType().equals(type)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "mismatched type " + type);
        }
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        Pagination<DeployConfigDTO> result = deployConfigProvider.list(request);
        return buildSucceedResult(result);
    }

    @Operation(summary = "删除指定类型的部署信息")
    @DeleteMapping("types/{type}")
    public TeslaBaseResult deleteByType(
            @PathVariable String type,
            @ModelAttribute DeployConfigDeleteReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        if (StringUtils.isEmpty(request.getAppId())) {
            request.setAppId("");
        }
        DeployConfigTypeId typeIdObj = DeployConfigTypeId.valueOf(request.getTypeId());
        if (!typeIdObj.getType().equals(type)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "mismatched type " + type);
        }
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        String namespaceId = container.getNamespaceId();
        String stageId = container.getStageId();
        request.setIsolateNamespaceId(namespaceId);
        request.setIsolateStageId(stageId);
        deployConfigProvider.delete(request);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
