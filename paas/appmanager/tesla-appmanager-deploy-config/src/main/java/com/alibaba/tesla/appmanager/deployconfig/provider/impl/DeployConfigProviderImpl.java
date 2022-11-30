package com.alibaba.tesla.appmanager.deployconfig.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.DeployConfigProvider;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.deployconfig.assembly.DeployConfigDtoConvert;
import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.service.DeployConfigService;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.*;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigApplyTemplateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 部署配置 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Service
@Slf4j
public class DeployConfigProviderImpl implements DeployConfigProvider {

    private final DeployConfigService deployConfigService;
    private final DeployConfigDtoConvert deployConfigDtoConvert;

    public DeployConfigProviderImpl(
            DeployConfigService deployConfigService, DeployConfigDtoConvert deployConfigDtoConvert) {
        this.deployConfigService = deployConfigService;
        this.deployConfigDtoConvert = deployConfigDtoConvert;
    }

    /**
     * 应用部署模板 (拆分 launch yaml 并分别应用保存)
     *
     * @param req 应用请求
     * @return 应用结果
     */
    @Override
    public DeployConfigApplyTemplateRes<DeployConfigDTO> applyTemplate(DeployConfigApplyTemplateReq req) {
        DeployConfigApplyTemplateRes<DeployConfigDO> res = deployConfigService.applyTemplate(req);
        return DeployConfigApplyTemplateRes.<DeployConfigDTO>builder()
                .items(res.getItems().stream()
                        .map(deployConfigDtoConvert::to)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 生成指定应用在指定部署参数下的 Application Configuration Yaml
     *
     * @param req 部署参数
     * @return 生成 Yaml 结果
     */
    @Override
    public DeployConfigGenerateRes generate(DeployConfigGenerateReq req) {
        return deployConfigService.generate(req);
    }

    /**
     * 根据指定查询条件获取列表（不支持继承）
     *
     * @param req 查询请求
     * @return 部署配置列表
     */
    @Override
    public Pagination<DeployConfigDTO> list(DeployConfigListReq req) {
        DeployConfigQueryCondition condition = new DeployConfigQueryCondition();
        ClassUtil.copy(req, condition);
        List<DeployConfigDO> results = deployConfigService.list(condition);
        log.info("list deploy config from database|condition={}|resultSize={}",
                JSONObject.toJSONString(condition), results.size());
        return Pagination.valueOf(results, deployConfigDtoConvert::to);
    }

    /**
     * 更新指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 更新请求
     * @return 更新后的对象
     */
    @Override
    public DeployConfigDTO upsert(DeployConfigUpsertReq req) {
        return deployConfigDtoConvert.to(deployConfigService.update(req));
    }

    /**
     * 删除指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 删除请求
     */
    @Override
    public void delete(DeployConfigDeleteReq req) {
        deployConfigService.delete(req);
    }
}
