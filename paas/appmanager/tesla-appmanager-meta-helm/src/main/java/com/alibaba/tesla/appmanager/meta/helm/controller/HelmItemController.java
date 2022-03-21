package com.alibaba.tesla.appmanager.meta.helm.controller;

import com.alibaba.tesla.appmanager.api.provider.HelmMetaProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.dto.HelmMetaDTO;
import com.alibaba.tesla.appmanager.domain.req.helm.HelmMetaCreateReq;
import com.alibaba.tesla.appmanager.domain.req.helm.HelmMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.helm.HelmMetaUpdateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/apps/{appId}/helm")
@Slf4j
public class HelmItemController extends AppManagerBaseController {

    @Autowired
    private HelmMetaProvider metaProvider;

    /**
     * @api {get} /apps/:appId/helm/:id 获取指定HELM组件详情
     * @apiName GetApplicationHelmMeta
     * @apiGroup 应用关联HELM API
     * @apiParam (Path Parameters) {String} appId 应用ID
     * @apiParam (Path Parameters) {Number} id HELM 组件主键ID
     */
    @GetMapping(value = "/{id}")
    public TeslaBaseResult get(@PathVariable String appId, @PathVariable Long id) {
        HelmMetaDTO result = metaProvider.get(id);
        return buildSucceedResult(result);
    }

    /**
     * @api {get} /apps/:appId/helm 获取应用HELM组件列表
     * @apiName GetApplicationHelmMetaList
     * @apiGroup 应用关联HELM组件 API
     * @apiParam (Path Parameters) {String} appId 应用ID
     * @apiParam (GET Parameters) {String} id HELM组件主键ID
     * @apiParam (GET Parameters) {String} name HELM组件名称
     * @apiParam (GET Parameters) {Number} page 当前页
     * @apiParam (GET Parameters) {Number} pageSize 每页大小
     */
    @GetMapping
    public TeslaBaseResult list(@PathVariable String appId, @ModelAttribute HelmMetaQueryReq request) {
        request.setAppId(appId);
        request.setWithBlobs(true);
        return buildSucceedResult(metaProvider.list(request));
    }

    /**
     * @api {post} /apps/:appId/helm 新增HELM组件
     * @apiName PostApplicationHelmMetaList
     * @apiGroup 应用关联HELM组件 API
     * @apiParam (Path Parameters) {String} appId 应用ID
     * @apiParam (JSON Body) {String} helmPackageId HELM组件包ID
     * @apiParam (JSON Body) {String} name HELM组件名称
     * @apiParam (JSON Body) {String="REPO","FILE","HUB"} packageType 包类型
     * @apiParam (JSON Body) {String} helmExt 扩展信息
     * @apiParam (JSON Body) {String} options 构建options信息
     * @apiParam (JSON Body) {String} description 描述信息
     */
    @PostMapping
    public TeslaBaseResult create(@PathVariable String appId, @RequestBody HelmMetaCreateReq request) {
        request.setAppId(appId);
        request.checkReq();
        HelmMetaDTO result = metaProvider.create(request);
        return buildSucceedResult(result);
    }

    /**
     * @api {put} /apps/:appId/helm/:id 更新指定HELM组件
     * @apiName PutApplicationHelmMetaList
     * @apiGroup 应用关联HELM组件 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (Path Parameters) {Number} id HELM组件主键 ID
     */
    @PutMapping(value = "/{id}")
    public TeslaBaseResult update(@PathVariable String appId, @PathVariable Long id,
                                  @RequestBody HelmMetaUpdateReq request) {
        request.setId(id);
        request.setAppId(appId);
        request.checkReq();
        HelmMetaDTO result = metaProvider.update(request);
        return buildSucceedResult(result);
    }

    /**
     * @api {delete} /apps/:appId/helm/:id 删除HELM组件
     * @apiName DeleteApplicationHelmMeta
     * @apiGroup 应用关联HELM组件 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (Path Parameters) {Number} id HELM组件主键 ID
     */
    @DeleteMapping(value = "/{id}")
    public TeslaBaseResult delete(@PathVariable String appId, @PathVariable Long id) {
        if (Objects.isNull(id)) {
            return buildSucceedResult(Boolean.TRUE);
        }
        return buildSucceedResult(metaProvider.delete(id));
    }
}
