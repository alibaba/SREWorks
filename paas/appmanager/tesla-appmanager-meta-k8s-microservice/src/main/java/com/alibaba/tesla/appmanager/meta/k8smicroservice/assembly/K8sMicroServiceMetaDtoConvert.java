package com.alibaba.tesla.appmanager.meta.k8smicroservice.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.K8sMicroServiceMetaDTO;
import com.alibaba.tesla.appmanager.meta.k8smicroservice.repository.domain.K8sMicroServiceMetaDO;

import org.springframework.stereotype.Component;

/**
 * 微应用 DTO 转换器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Component
public class K8sMicroServiceMetaDtoConvert extends BaseDtoConvert<K8sMicroServiceMetaDTO, K8sMicroServiceMetaDO> {

    public K8sMicroServiceMetaDtoConvert() {
        super(K8sMicroServiceMetaDTO.class, K8sMicroServiceMetaDO.class);
    }
}
