package com.alibaba.tesla.appmanager.domain.req;

import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.domain.dto.ImagePushDTO;
import com.alibaba.tesla.appmanager.domain.dto.InitContainerDTO;
import com.alibaba.tesla.appmanager.domain.dto.LaunchDTO;
import com.alibaba.tesla.appmanager.domain.dto.RepoDTO;
import lombok.Data;

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
     * 类型
     */
    private String kind;

    /**
     * 组件类型
     */
    private ComponentTypeEnum componentType;

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
