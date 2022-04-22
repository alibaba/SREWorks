package com.alibaba.tesla.appmanager.meta.k8smicroservice.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.K8sMicroServiceMetaProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.ContainerTypeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.dto.ContainerObjectDTO;
import com.alibaba.tesla.appmanager.domain.dto.K8sMicroServiceMetaDTO;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQuickUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaUpdateReq;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

/**
 * K8S 微服务元信息 Controller
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@RequestMapping("/apps/{appId}/k8s-microservices")
@RestController
@Slf4j
public class K8sMicroserviceItemController extends AppManagerBaseController {

    private static Map<String, String> SERVICE_TYPE_2_LANGUAGE = ImmutableMap.<String, String>builder()
            .put("SpringBoot", "java")
            .put("Web.Py", "python2")
            .build();

    @Autowired
    private K8sMicroServiceMetaProvider metaProvider;

    /**
     * @api {get} /apps/:appId/k8s-microservices 获取微服务列表
     * @apiName GetApplicationK8sMicroserviceList
     * @apiGroup 应用关联微服务 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (GET Parameters) {String[]} componentTypeList 微服务类型列表 (可选 MICROSERVICE, K8S_MICROSERVICE, K8S_JOB)
     * @apiParam (GET Parameters) {Number} page 当前页
     * @apiParam (GET Parameters) {Number} pageSize 每页大小
     */
    @GetMapping
    public TeslaBaseResult list(@PathVariable String appId, @ModelAttribute K8sMicroServiceMetaQueryReq request) {
        request.setAppId(appId);
        request.setWithBlobs(true);
        return buildSucceedResult(metaProvider.list(request));
    }

    /**
     * @api {post} /apps/:appId/k8s-microservices 新增微服务
     * @apiName PostApplicationK8sMicroservices
     * @apiGroup 应用关联微服务 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (JSON Body) {String} microServiceId 微服务标识 ID
     * @apiParam (JSON Body) {String} name 微服务名称
     * @apiParam (JSON Body) {String} description 描述信息
     * @apiParam (JSON Body) {Object[]} containerObjectList 容器对象列表
     * @apiParam (JSON Body) {Object[]} envList 环境变量列表
     * @apiParam (JSON Body) {String="K8S_MICROSERVICE","K8S_JOB"} componentType 组件类型
     */
    @PostMapping
    public TeslaBaseResult create(@PathVariable String appId, @RequestBody K8sMicroServiceMetaUpdateReq request) {
        repaire(request);
        request.setAppId(appId);
        K8sMicroServiceMetaDTO result;
        try {
            result = metaProvider.create(request);
        } catch (AppException e) {
            if (AppErrorCode.GIT_ERROR.equals(e.getErrorCode())) {
                return buildClientErrorResult("abc" + e.getErrorMessage());
            }
            throw e;
        }
        return buildSucceedResult(result);
    }

    @PostMapping("quick-create")
    public TeslaBaseResult quickCreate(
            @PathVariable String appId, @RequestBody K8sMicroServiceMetaQuickUpdateReq request) {
        if (StringUtils.isEmpty(request.getKind())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "kind parameter is required");
        }
        request.setAppId(appId);
        request.setComponentType(ComponentTypeEnum.K8S_MICROSERVICE);
        request.setName(request.getMicroServiceId());
        K8sMicroServiceMetaDTO result = metaProvider.create(request);
        return buildSucceedResult(result);
    }

    @PostMapping("quick-update")
    public TeslaBaseResult quickUpdate(
            @PathVariable String appId, @RequestBody K8sMicroServiceMetaQuickUpdateReq request) {
        if (StringUtils.isEmpty(request.getKind())) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "kind parameter is required");
        }
        request.setAppId(appId);
        request.setComponentType(ComponentTypeEnum.K8S_MICROSERVICE);
        request.setName(request.getMicroServiceId());
        K8sMicroServiceMetaDTO result = metaProvider.update(request);
        return buildSucceedResult(result);
    }

    @PostMapping(params = {"type"})
    public TeslaBaseResult createByYaml(
            @PathVariable String appId,
            @RequestParam("type") String type,
            @RequestBody String body) {
        if (StringUtils.isEmpty(type) || !type.equals("yaml")) {
            return buildClientErrorResult("invalid type parameter");
        }

        Yaml yaml = SchemaUtil.createYaml(Arrays.asList(Iterable.class, Object.class));
        Iterable<Object> iterable = yaml.loadAll(body);
        List<K8sMicroServiceMetaUpdateReq> k8sMicroServiceMetaUpdateReqList = new ArrayList<>();
        for (Object object : iterable) {
            JSONObject root = new JSONObject((Map) object);
            JSONObject options = root.getJSONObject("options");
            String componentName = root.getString("componentName");
            ComponentTypeEnum componentType = ComponentTypeEnum.valueOf(root.getString("componentType"));
            K8sMicroServiceMetaUpdateReq k8sMicroServiceMetaUpdateReq = new K8sMicroServiceMetaUpdateReq();

            K8sMicroServiceMetaDO k8sMicroServiceMetaDO = new K8sMicroServiceMetaDO();
            k8sMicroServiceMetaDO.fromOptions(options);
            k8sMicroServiceMetaUpdateReq.setEnvList(k8sMicroServiceMetaDO.getEnvList());
            k8sMicroServiceMetaUpdateReq.setContainerObjectList(k8sMicroServiceMetaDO.getContainerObjectList());
            k8sMicroServiceMetaUpdateReq.setAppId(appId);
            k8sMicroServiceMetaUpdateReq.setMicroServiceId(componentName);
            k8sMicroServiceMetaUpdateReq.setName(componentName);
            k8sMicroServiceMetaUpdateReq.setComponentType(componentType);
            k8sMicroServiceMetaUpdateReqList.add(k8sMicroServiceMetaUpdateReq);
        }

        for (K8sMicroServiceMetaUpdateReq k8sMicroServiceMetaUpdateReq : k8sMicroServiceMetaUpdateReqList) {
            metaProvider.create(k8sMicroServiceMetaUpdateReq);
        }
        return buildSucceedResult(k8sMicroServiceMetaUpdateReqList.size());
    }

    /**
     * @api {get} /apps/:appId/k8s-microservices/:id 获取指定微服务详情
     * @apiName GetApplicationK8sMicroservice
     * @apiGroup 应用关联微服务 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (Path Parameters) {Number} id 微服务主键 ID
     */
    @GetMapping(value = "/{id}")
    public TeslaBaseResult get(@PathVariable String appId, @PathVariable Long id) {
        K8sMicroServiceMetaDTO result = metaProvider.get(id);
        return buildSucceedResult(result);
    }

    /**
     * @api {put} /apps/:appId/k8s-microservices/:id 更新指定微服务详情
     * @apiName PutApplicationK8sMicroservice
     * @apiGroup 应用关联微服务 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (Path Parameters) {Number} id 微服务主键 ID
     * @apiParam (JSON Body) {String} name 微服务名称
     * @apiParam (JSON Body) {String} description 描述信息
     * @apiParam (JSON Body) {Object[]} containerObjectList 容器对象列表
     * @apiParam (JSON Body) {Object[]} envList 环境变量列表
     * @apiParam (JSON Body) {String="K8S_MICROSERVICE","K8S_JOB"} componentType 组件类型
     */
    @PutMapping(value = "/{id}")
    public TeslaBaseResult update(
            @PathVariable String appId,
            @PathVariable Long id,
            @RequestBody K8sMicroServiceMetaUpdateReq request) {
        repaire(request);
        request.setId(id);
        request.setAppId(appId);
        K8sMicroServiceMetaDTO result = metaProvider.update(request);
        return buildSucceedResult(result);
    }

    /**
     * @api {delete} /apps/:appId/k8s-microservices/:id 删除指定微服务详情
     * @apiName DeleteApplicationK8sMicroservice
     * @apiGroup 应用关联微服务 API
     * @apiParam (Path Parameters) {String} appId 应用 ID
     * @apiParam (Path Parameters) {Number} id 微服务主键 ID
     */
    @DeleteMapping(value = "/{id}")
    public TeslaBaseResult delete(@PathVariable String appId, @PathVariable Long id) {
        if (Objects.isNull(id)) {
            return buildSucceedResult(Boolean.TRUE);
        }
        JSONObject result = new JSONObject();
        result.put("id", id);
        result.put("result", metaProvider.delete(id));
        return buildSucceedResult(result);
    }

    private void repaire(K8sMicroServiceMetaUpdateReq request) {
        List<ContainerObjectDTO> containerObjectList = request.getContainerObjectList();
        if (request.getComponentType() == ComponentTypeEnum.K8S_MICROSERVICE) {
            ContainerObjectDTO mainContainer = containerObjectList.stream().filter(
                            containerObject -> containerObject.getContainerType() == ContainerTypeEnum.CONTAINER).findFirst()
                    .orElse(null);

            if (Objects.isNull(mainContainer)) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "Container 缺失");
            }
            mainContainer.setName(request.getMicroServiceId());

            containerObjectList.stream().filter(
                    container -> container.getContainerType() == ContainerTypeEnum.INIT_CONTAINER).forEach(container -> {
                container.setRepo(mainContainer.getRepo());
                container.setBranch(mainContainer.getBranch());
            });
        } else if (request.getComponentType() == ComponentTypeEnum.K8S_JOB) {
            if (CollectionUtils.size(containerObjectList) != 1) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "JOB 缺失");
            }

            ContainerObjectDTO jobContainer = containerObjectList.get(0);
            jobContainer.setName(request.getMicroServiceId());
        }

        containerObjectList.stream().filter(container -> StringUtils.isNotEmpty(container.getServiceType())).forEach(
                container -> container.setLanguage(SERVICE_TYPE_2_LANGUAGE.get(container.getServiceType())));
    }
}
