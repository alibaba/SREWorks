package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.*;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigApplyTemplateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;

/**
 * 部署配置 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface DeployConfigProvider {

    /**
     * 应用部署模板 (拆分 launch yaml 并分别应用保存)
     *
     * @param req 应用请求
     * @return 应用结果
     */
    DeployConfigApplyTemplateRes<DeployConfigDTO> applyTemplate(DeployConfigApplyTemplateReq req);

    /**
     * 生成指定应用在指定部署参数下的 Application Configuration Yaml
     *
     * @param req 部署参数
     * @return 生成 Yaml 结果
     */
    DeployConfigGenerateRes generate(DeployConfigGenerateReq req);

    /**
     * 根据指定查询条件获取列表（不支持继承）
     *
     * @param req 查询请求
     * @return 部署配置列表
     */
    Pagination<DeployConfigDTO> list(DeployConfigListReq req);

    /**
     * 更新指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 更新请求
     * @return 更新后的对象
     */
    DeployConfigDTO upsert(DeployConfigUpsertReq req);

    /**
     * 删除指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 删除请求
     */
    void delete(DeployConfigDeleteReq req);
}
