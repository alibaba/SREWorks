package com.alibaba.tesla.appmanager.kubernetes.sevice.kubectl;

import com.alibaba.tesla.appmanager.domain.req.kubectl.*;

/**
 * Kubectl 服务
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface KubectlService {

    /**
     * 应用 Yaml
     */
    void apply(KubectlApplyReq request, String empId);

    /**
     * 删除 Deployment
     */
    void deleteDeployment(KubectlDeleteDeploymentReq request, String empId);

    /**
     * 删除 StatefulSet
     */
    void deleteStatefulSet(KubectlDeleteStatefulSetReq request, String empId);

    /**
     * 删除 Job
     */
    void deleteJob(KubectlDeleteJobReq request, String empId);
}
