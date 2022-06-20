package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.MaintainerProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.domain.container.BizAppContainer;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Addon 元信息 Controller
 *
 * @author qiuqiang.qq@alibaba-inc.com
 */
@RequestMapping("/maintainer")
@RestController
@Slf4j
public class MaintainerController extends AppManagerBaseController {

    @Autowired
    private MaintainerProvider maintainerProvider;

    /**
     * 升级 namespaceId / stageId (针对各 meta 表新增的 namespaceId / stageId 空字段进行初始化)
     *
     * @param headerBizApp X-Biz-App Header
     * @return Response
     */
    @PostMapping("/upgradeNamespaceStage")
    public TeslaBaseResult upgradeNamespaceStage(
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp) {
        BizAppContainer container = BizAppContainer.valueOf(headerBizApp);
        String namespaceId = container.getNamespaceId();
        String stageId = container.getStageId();
        maintainerProvider.upgradeNamespaceStage(namespaceId, stageId);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }
}
