package com.alibaba.tesla.appmanager.domain.req;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.util.SecurityUtil;
import com.alibaba.tesla.appmanager.domain.dto.*;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * K8S 微服务元数据快速变更请求
 *
 * @author qianmo.zm@alibaba-inc.com
 */
@Data
public class K8sMicroServiceMetaQuickUpdateReq {

    /**
     * 主键 ID
     */
    @Deprecated
    private Long id;

    /**
     * 应用标示
     */
    @Deprecated
    private String appId;

    /**
     * Namespace ID
     */
    private String namespaceId;

    /**
     * Stage ID
     */
    private String stageId;

    /**
     * 微服务标示
     */
    private String microServiceId;

    /**
     * 微服务名称
     */
    private String name;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 组件类型
     */
    private String componentType;

    /**
     * 架构
     */
    private String arch;

    /**
     * 类型 (Deployment/StatefulSet/CloneSet/AdvancedStatefulSet)
     */
    private String kind;

    /**
     * 环境变量定义
     */
    private List<EnvMetaDTO> envList;

    /**
     * 构建对象
     */
    private List<ContainerObjectDTO> containerObjectList;

    /**
     * 初始化容器对象
     */
    private List<InitContainerDTO> initContainerList;

    /**
     * 环境变量Key定义
     */
    private List<String> envKeyList;

    /**
     * 仓库配置
     */
    private RepoDTO repoObject;

    /**
     * 镜像推送配置
     */
    private ImagePushDTO imagePushObject;

    /**
     * 部署对象
     */
    private LaunchDTO launchObject;

    /**
     * 归属产品 ID
     */
    private String productId;

    /**
     * 归属发布版本 ID
     */
    private String releaseId;

    /**
     * 检查参数合法性
     */
    public void checkParameters() {
        SecurityUtil.checkInput(appId);
        SecurityUtil.checkInput(namespaceId);
        SecurityUtil.checkInput(stageId);
        SecurityUtil.checkInput(microServiceId);
        SecurityUtil.checkInput(name);
        SecurityUtil.checkInput(componentType);
        SecurityUtil.checkInput(arch);
        SecurityUtil.checkInput(kind);
        if (!CollectionUtils.isEmpty(envList)) {
            envList.forEach(item -> item.checkParameters());
        }
        if (!CollectionUtils.isEmpty(containerObjectList)) {
            containerObjectList.forEach(item -> item.checkParameters());
        }
        if (!CollectionUtils.isEmpty(initContainerList)) {
            initContainerList.forEach(item -> item.checkParameters());
        }
        if (repoObject != null) {
            repoObject.checkParameters();
        }
        if (imagePushObject != null) {
            imagePushObject.checkParameters();
        }
        if (launchObject != null) {
            launchObject.checkParameters();
        }
        SecurityUtil.checkInput(productId);
        SecurityUtil.checkInput(releaseId);
    }
}
