package com.alibaba.tesla.appmanager.domain.req.cluster;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Cluster 查询请求
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClusterQueryReq implements Serializable {

    private static final long serialVersionUID = -1872143932301240289L;

    /**
     * Cluster ID
     */
    private String clusterId;

    /**
     * Cluster 名称
     */
    private String clusterName;
}
