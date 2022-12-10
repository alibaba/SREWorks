package com.alibaba.tesla.appmanager.server.service.appenvironment.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.EnvUtil;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;
import com.alibaba.tesla.appmanager.domain.req.productrelease.ProductReleaseAppUpdateReq;
import com.alibaba.tesla.appmanager.meta.helm.service.HelmMetaService;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.service.K8sMicroserviceMetaService;
import com.alibaba.tesla.appmanager.server.assembly.ProductDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.domain.ProductDO;
import com.alibaba.tesla.appmanager.server.repository.domain.ProductReleaseAppRelDO;
import com.alibaba.tesla.appmanager.server.service.appcomponent.AppComponentService;
import com.alibaba.tesla.appmanager.server.service.appenvironment.AppEnvironmentService;
import com.alibaba.tesla.appmanager.server.service.productrelease.ProductReleaseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 应用环境服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class AppEnvironmentServiceImpl implements AppEnvironmentService {

    private final ProductReleaseService productReleaseService;
    private final DeployConfigService deployConfigService;
    private final ProductDtoConvert productDtoConvert;

    public AppEnvironmentServiceImpl(
            ProductReleaseService productReleaseService,
            DeployConfigService deployConfigService,
            ProductDtoConvert productDtoConvert) {
            this.productReleaseService = productReleaseService;
        this.deployConfigService = deployConfigService;
        this.productDtoConvert = productDtoConvert;
    }

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req 加入环境请求
     */
    @Override
    public void bindEnvironment(AppEnvironmentBindReq req) {
        String appId = req.getAppId();
        String isolateNamespaceId = req.getIsolateNamespaceId();
        String isolateStageId = req.getIsolateStageId();
        String productId = req.getProductId();
        String releaseId = req.getReleaseId();
        String baselineBranch = req.getBaselineBranch();
        String operator = req.getOperator();
        if (StringUtils.isAnyEmpty(appId, isolateNamespaceId, isolateStageId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "appId/isolateNamespaceId/isolateStageId are required");
        }
        if (StringUtils.isAnyEmpty(req.getUnitId(), req.getNamespaceId())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "unitId/stageId are required");
        }
        if (StringUtils.isAnyEmpty(productId, releaseId, baselineBranch, operator)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "productId/releaseId/baselineBranch/operator are required");
        }
        String envId = EnvUtil.generate(req.getUnitId(), req.getClusterId(), req.getNamespaceId(), req.getStageId());

        // 检查当前 ProductReleaseAppRel 表中的引用是否存在，不存在则新增
        String filePath;
        if (StringUtils.isEmpty(req.getStageId())) {
            filePath = String.format("%s/%s/%s.yaml", appId, req.getUnitId(), req.getNamespaceId());
        } else {
            filePath = String.format("%s/%s/%s_%s.yaml", appId, req.getUnitId(), req.getNamespaceId(), req.getStageId());
        }
        ProductDO product = productReleaseService.getProduct(productId);
        if (product == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find product by productId %s", productId));
        }
        ProductReleaseAppRelDO appRel = productReleaseService.updateAppRel(ProductReleaseAppUpdateReq.builder()
                .productId(productId)
                .releaseId(releaseId)
                .appId(appId)
                .tag("")
                .branch(baselineBranch)
                .buildPath("")
                .launchPath(filePath)
                .build());
        log.info("update product release app rel successfully|productId={}|releaseId={}|appId={}|baselineBranch={}|" +
                        "isolateNamespaceId={}|isolateStageId={}|filePath={}|envId={}|operator={}|res={}", productId,
                releaseId, appId, baselineBranch, isolateNamespaceId, isolateStageId, filePath, envId, operator,
                JSONObject.toJSONString(appRel));

        // 绑定实际的环境，并同步到 Git 中
        deployConfigService.bindEnvironment(req, productDtoConvert.to(product), filePath);
    }
}
