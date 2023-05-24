package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.AppComponentProvider;
import com.alibaba.tesla.appmanager.api.provider.AppMetaProvider;
import com.alibaba.tesla.appmanager.api.provider.AppVersionProvider;
import com.alibaba.tesla.appmanager.api.provider.DeployConfigProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.container.BizAppContainer;
import com.alibaba.tesla.appmanager.domain.dto.AppMetaDTO;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import com.alibaba.tesla.appmanager.domain.req.AppMetaCreateReq;
import com.alibaba.tesla.appmanager.domain.req.AppMetaDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.AppMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.AppMetaUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigApplyTemplateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigGenerateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigListReq;
import com.alibaba.tesla.appmanager.domain.res.appmeta.AppGetVersionRes;
import com.alibaba.tesla.appmanager.domain.res.apppackage.ApplicationConfigurationGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 应用元信息 Controller
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Tag(name = "应用 API")
@RequestMapping("/apps")
@RestController
@Slf4j
public class AppController extends AppManagerBaseController {

    @Autowired
    private AppMetaProvider appMetaProvider;

    @Autowired
    private DeployConfigProvider deployConfigProvider;

    @Autowired
    private AppComponentProvider appComponentProvider;

    @Autowired
    private AppVersionProvider appVersionProvider;

    @Operation(summary = "查询应用列表")
    @GetMapping
    public TeslaBaseResult list(AppMetaQueryReq request, OAuth2Authentication auth) {
        Pagination<AppMetaDTO> pagination = appMetaProvider.list(request, getOperator(auth), false);
        return buildSucceedResult(pagination);
    }

    @Operation(summary = "创建应用")
    @PostMapping
    public TeslaBaseResult create(
            @RequestBody AppMetaCreateReq request,
            @RequestHeader(value = "X-EmpId", required = false) String empId,
            OAuth2Authentication auth) {
        String operator = getOperator(auth, empId);
        AppMetaDTO result = appMetaProvider.create(request, operator);
        return buildSucceedResult(result);
    }

    @Operation(summary = "查询应用详情")
    @GetMapping(value = "/{appId}")
    public TeslaBaseResult get(@PathVariable String appId, OAuth2Authentication auth) {
        AppMetaDTO result = appMetaProvider.get(appId, getOperator(auth));
        if (result == null) {
            return buildClientErrorResult(String.format(
                    "cannot find app %s or you don't have the permission to access", appId));
        }
        return buildSucceedResult(result);
    }

    @Operation(summary = "查询应用版本 (Frontend)")
    @GetMapping(value = "/{appId}/version")
    public TeslaBaseResult getFrontendVersion(@PathVariable String appId, OAuth2Authentication auth) {
        String version = appMetaProvider.getFrontendVersion(appId, getOperator(auth));
        return buildSucceedResult(AppGetVersionRes.builder().version(version).build());
    }

    @Operation(summary = "更新应用详情")
    @PutMapping(value = "/{appId}")
    public TeslaBaseResult update(
            @PathVariable String appId,
            @RequestBody AppMetaUpdateReq request,
            @RequestHeader(value = "X-EmpId", required = false) String empId,
            OAuth2Authentication auth) {
        String operator = getOperator(auth, empId);
        request.setAppId(appId);
        AppMetaDTO result = appMetaProvider.update(request, operator);
        return buildSucceedResult(result);
    }

    @Operation(summary = "更新应用详情 (兼容API)")
    @PutMapping
    public TeslaBaseResult updateCompatible(@RequestBody AppMetaUpdateReq request, OAuth2Authentication auth) {
        AppMetaDTO result = appMetaProvider.update(request, getOperator(auth));
        return buildSucceedResult(result);
    }

    @Operation(summary = "删除应用")
    @DeleteMapping(value = "/{appId}")
    public TeslaBaseResult delete(
            @PathVariable String appId,
            @ParameterObject @ModelAttribute AppMetaDeleteReq request,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(appId)) {
            return buildSucceedResult(Boolean.TRUE);
        }
        String cloudType = System.getenv("CLOUD_TYPE");
        if ("Internal".equals(cloudType)) {
            return buildClientErrorResult("Deleting apps is now prohibited");
        }

        request.setAppId(appId);
        boolean result = appMetaProvider.delete(request, getOperator(auth));
        appVersionProvider.clean(appId, getOperator(auth));
        if (request.getRemoveAllDeployConfigs()) {
            DeployConfigListReq deployConfigRequest = DeployConfigListReq.builder()
                    .appId(request.getAppId())
                    .build();
            Pagination<DeployConfigDTO> deployConfigs = deployConfigProvider.list(deployConfigRequest);
            for (DeployConfigDTO deployConfigDTO : deployConfigs.getItems()) {
                deployConfigProvider.delete(
                        DeployConfigDeleteReq.builder()
                            .appId(deployConfigDTO.getAppId())
                            .apiVersion(deployConfigDTO.getApiVersion())
                            .typeId(deployConfigDTO.getTypeId())
                            .envId(deployConfigDTO.getEnvId())
                            .isolateNamespaceId(deployConfigDTO.getNamespaceId())
                            .isolateStageId(deployConfigDTO.getStageId())
                            .build()
                );
            }
        }
        return buildSucceedResult(result);
    }

    @Operation(summary = "更新应用部署信息")
    @PutMapping(value = "/{appId}/application-configurations")
    public TeslaBaseResult updateApplicationConfigurations(
            @PathVariable String appId,
            @RequestBody DeployConfigApplyTemplateReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        request.setAppId(appId);

        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        return buildSucceedResult(deployConfigProvider.applyTemplate(request));
    }

    @Operation(summary = "查询应用部署信息")
    @GetMapping(value = "/{appId}/application-configurations")
    public TeslaBaseResult getApplicationConfigurations(
            @PathVariable String appId,
            @ParameterObject @ModelAttribute DeployConfigGenerateReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        request.setAppId(appId);
        request.setAppPackageId(0L);

        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        request.setAppComponents(appComponentProvider
                .getFullComponentRelations(appId, container.getNamespaceId(), container.getStageId()));
        DeployConfigGenerateRes result = deployConfigProvider.generate(request);
        return buildSucceedResult(ApplicationConfigurationGenerateRes.builder()
                .yaml(SchemaUtil.toYamlMapStr(result.getSchema()))
                .build());
    }

    @Operation(summary = "删除应用部署信息")
    @DeleteMapping(value = "/{appId}/application-configurations")
    public TeslaBaseResult deleteApplicationConfigurations(
            @PathVariable String appId,
            @ParameterObject @ModelAttribute DeployConfigDeleteReq request,
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp,
            OAuth2Authentication auth) {
        if (StringUtils.isEmpty(request.getApiVersion())) {
            request.setApiVersion(DefaultConstant.API_VERSION_V1_ALPHA2);
        }
        request.setAppId(appId);

        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        request.setIsolateNamespaceId(container.getNamespaceId());
        request.setIsolateStageId(container.getStageId());
        deployConfigProvider.delete(request);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
