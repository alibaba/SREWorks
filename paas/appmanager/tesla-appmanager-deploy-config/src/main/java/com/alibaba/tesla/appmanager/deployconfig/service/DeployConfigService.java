package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.tesla.appmanager.deployconfig.repository.condition.DeployConfigQueryCondition;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.domain.dto.ProductDTO;
import com.alibaba.tesla.appmanager.domain.req.appenvironment.AppEnvironmentBindReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.*;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigApplyTemplateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigGenerateRes;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigSyncToGitBaselineRes;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;

import java.util.List;

/**
 * 部署配置服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface DeployConfigService {

    /**
     * 应用部署模板 (拆分 launch yaml 并分别应用保存)
     *
     * @param req 应用请求
     * @return 应用结果
     */
    DeployConfigApplyTemplateRes<DeployConfigDO> applyTemplate(DeployConfigApplyTemplateReq req);

    /**
     * 根据指定查询条件获取列表（不支持继承）
     *
     * @param condition 查询请求
     * @return 部署配置列表
     */
    List<DeployConfigDO> list(DeployConfigQueryCondition condition);

    /**
     * 根据指定查询条件获取对应配置记录（支持继承）
     *
     * @param condition 查询请求
     * @return 部署配置列表
     */
    DeployConfigDO getWithInherit(DeployConfigQueryCondition condition);

    /**
     * 更新指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 更新请求
     * @return 更新后的对象
     */
    DeployConfigDO update(DeployConfigUpsertReq req);

    /**
     * 删除指定 apiVersion + appId + typeId + envId 对应的 DeployConfig 记录
     *
     * @param req 删除请求
     */
    void delete(DeployConfigDeleteReq req);

    /**
     * 生成指定应用在指定部署参数下的 Application Configuration Yaml
     *
     * @param req 部署参数
     * @return 生成 Yaml 结果
     */
    DeployConfigGenerateRes generate(DeployConfigGenerateReq req);

    /**
     * 将当前的 Application 同步到指定仓库基线中
     *
     * @param req 当前 Application 及目标仓库路径
     */
    DeployConfigSyncToGitBaselineRes syncToGitBaseline(DeployConfigSyncToGitBaselineReq req);

    /**
     * 将指定应用加入到指定环境中
     *
     * @param req      加入环境请求
     * @param product  产品对象
     * @param filePath 目标路径
     */
    void bindEnvironment(AppEnvironmentBindReq req, ProductDTO product, String filePath);

    /**
     * 获取当前的指定隔离环境下的全局模板清单
     *
     * @param apiVersion         API 版本
     * @param envId              目标环境 ID
     * @param isolateNamespaceId 隔离 Namespace ID
     * @param isolateStageId     隔离 Stage ID
     * @return Deploy Config 记录列表 (如果当前全局模板不合法则抛出异常)
     */
    List<DeployConfigDO> getGlobalTemplate(
            String apiVersion, String envId, String isolateNamespaceId, String isolateStageId);

    /**
     * 获取指定应用在指定环境中的默认 Application Configuration 模板
     *
     * @param req 请求参数 (全部参数必填项)
     * @return Application Configuration
     */
    DeployAppSchema getDefaultTemplate(DeployConfigGetDefaultTemplateReq req);

    /**
     * 针对目标 Scope 进行 Cluster/Namespace/Stage 覆盖
     *
     * @param req    请求
     * @param schema ApplicationConfiguration 中的 SpecComponent Schema
     * @return DeployAppSchema.SpecComponent
     */
    DeployAppSchema.SpecComponent enrichComponentScopes(
            DeployConfigGenerateReq req, DeployAppSchema.SpecComponent schema);

    /**
     * 根据指定条件寻找最佳部署配置
     *
     * @param records  根 deploy config 配置 (无 appId，全局配置)
     * @param clusterId   集群 ID
     * @param namespaceId Namespace ID
     * @param stageId     Stage ID
     * @return 最佳配置记录
     */
    DeployConfigDO findBestConfigInRecordsByGeneralType(
            List<DeployConfigDO> records, String clusterId, String namespaceId, String stageId);

    /**
     * 根据指定条件寻找最佳部署配置
     *
     * @param appRecords        指定应用下的 deploy config 配置
     * @param rootRecords       根 deploy config 配置 (无 appId, 全局配置)
     * @param rootParentRecords rootRecords 的 parent type id 对应的过滤 deploy config 配置 (无 appId, 全局配置)
     * @param unitId            单元 ID
     * @param clusterId         集群 ID
     * @param namespaceId       Namespace ID
     * @param stageId           Stage ID
     * @return 最佳配置记录 (如果返回 null，表明当前无异常，但无法由给定条件找到合理数据，应当由上层忽略此次查找)
     */
    DeployConfigDO findBestConfigInRecordsBySpecifiedName(
            List<DeployConfigDO> appRecords, List<DeployConfigDO> rootRecords, List<DeployConfigDO> rootParentRecords,
            String unitId, String clusterId, String namespaceId, String stageId);
}
