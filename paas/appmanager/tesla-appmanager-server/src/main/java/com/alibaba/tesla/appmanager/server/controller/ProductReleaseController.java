package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.server.job.ProductReleaseSchedulerJob;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

/**
 * 产品发布版本管理
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "产品发布版本 API")
@RequestMapping("/products/{productId:.+}/releases/{releaseId:.+}")
@RestController
public class ProductReleaseController extends AppManagerBaseController {

    @Autowired
    private ProductReleaseSchedulerJob productReleaseSchedulerJob;

    @PostMapping
    public TeslaBaseResult trigger(
            @PathVariable("productId") @NotEmpty String productId,
            @PathVariable("releaseId") @NotEmpty String releaseId,
            HttpServletRequest r, OAuth2Authentication auth) {
        productReleaseSchedulerJob.trigger(productId, releaseId);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
