package com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.condition;

import java.util.List;

import com.alibaba.tesla.appmanager.common.BaseCondition;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 应用元信息查询条件类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class K8sMicroserviceMetaQueryCondition extends BaseCondition {
    private Long id;

    private String appId;

    private String microServiceId;

    private List<ComponentTypeEnum> componentTypeList;

}
