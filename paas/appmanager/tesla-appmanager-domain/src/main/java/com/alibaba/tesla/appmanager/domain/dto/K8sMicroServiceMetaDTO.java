package com.alibaba.tesla.appmanager.domain.dto;

import java.util.Date;
import java.util.List;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;

import lombok.Data;

/**
 * @author qianmo.zm@alibaba-inc.com
 * @date 2020/09/28.
 */
@Data
public class K8sMicroServiceMetaDTO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private Date gmtCreate;

    /**
     * 修改时间
     */
    private Date gmtModified;

    /**
     * 应用标示
     */
    private String appId;

    /**
     * 微服务标示
     */
    private String microServiceId;

    /**
     * 微服务名称
     */
    private String name;

    /**
     * 微服务描述
     */
    private String description;

    /**
     * 类型 (Deployment/StatefulSet/CloneSet/AdvancedStatefulSet)
     */
    private String kind;

    /**
     * 组件类型
     */
    private ComponentTypeEnum componentType;

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
}
