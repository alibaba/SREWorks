package com.alibaba.tesla.appmanager.domain.req.deployconfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 在指定条件下是否存在 Type:envBinding 项
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployConfigHasEnvBindingReq {

    private String apiVersion;

    private String appId;

    private String isolateNamespaceId;

    private String isolateStageId;
}
