package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.MaintainerProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.EnvUtil;
import com.alibaba.tesla.appmanager.server.repository.condition.RtComponentInstanceQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.RtComponentInstanceDO;
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Addon 元信息 Controller
 *
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Tag(name = "系统维护 API")
@RequestMapping("/maintainer")
@RestController
@Slf4j
public class MaintainerController extends AppManagerBaseController {

    @Autowired
    private MaintainerProvider maintainerProvider;

    @Autowired
    private RtComponentInstanceService componentInstanceService;

    /**
     * 升级 namespaceId / stageId (针对各 meta 表新增的 namespaceId / stageId 空字段进行初始化)
     *
     * @param headerBizApp X-Biz-App Header
     * @return Response
     */
    @PostMapping("/upgradeNamespaceStage")
    public TeslaBaseResult upgradeNamespaceStage(
            @RequestHeader(value = "X-Biz-App", required = false) String headerBizApp) {
        if (!EnvUtil.isSreworks()) {
            log.info("not sreworks environment, abort");
            return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
        }

        String[] array = headerBizApp.split(",", 3);
        String namespaceId = array[1];
        String stageId = array[2];
        maintainerProvider.upgradeNamespaceStage(namespaceId, stageId);
        return buildSucceedResult(DefaultConstant.EMPTY_OBJ);
    }

    /**
     * 对于历史没有 ComponentSchema 字段的组件实例，刷新当前最新的 ComponentSchema 进去
     *
     * @return Response
     */
    @PostMapping("/refreshComponentSchema")
    public TeslaBaseResult refreshComponentSchema(
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Pagination<RtComponentInstanceDO> records = componentInstanceService.list(
                RtComponentInstanceQueryCondition.builder()
                        .emptyComponentSchema(true)
                        .page(1)
                        .pageSize(size)
                        .pagination(true)
                        .build());
        List<String> logs = componentInstanceService.refreshComponentSchema(records.getItems());
        return buildSucceedResult(logs);
    }
}
