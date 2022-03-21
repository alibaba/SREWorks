package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.K8sMicroServiceMetaDTO;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQueryReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaQuickUpdateReq;
import com.alibaba.tesla.appmanager.domain.req.K8sMicroServiceMetaUpdateReq;

/**
 * K8S 微应用元信息接口
 *
 * @author qianmo.zm@alibaba-inc.com
 */
public interface K8sMicroServiceMetaProvider {

    /**
     * 分页查询微应用元信息
     */
    Pagination<K8sMicroServiceMetaDTO> list(K8sMicroServiceMetaQueryReq request);

    /**
     * 通过微应用 ID 查询微应用元信息
     */
    K8sMicroServiceMetaDTO get(Long id);

    /**
     * 通过微应用 ID 删除微应用元信息
     */
    boolean delete(Long id);

    /**
     * 创建 K8S Microservice
     */
    K8sMicroServiceMetaDTO create(K8sMicroServiceMetaUpdateReq request);

    /**
     * 更新 K8s Microservice
     */
    K8sMicroServiceMetaDTO update(K8sMicroServiceMetaUpdateReq request);

    /**
     * 快速创建 K8S Microservice
     */
    K8sMicroServiceMetaDTO create(K8sMicroServiceMetaQuickUpdateReq request);

    /**
     * 快速更新 K8S Microservice
     */
    K8sMicroServiceMetaDTO update(K8sMicroServiceMetaQuickUpdateReq request);
}
