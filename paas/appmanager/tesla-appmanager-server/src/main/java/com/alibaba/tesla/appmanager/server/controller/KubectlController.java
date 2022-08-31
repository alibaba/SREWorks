package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.req.kubectl.*;
import com.alibaba.tesla.appmanager.kubernetes.sevice.kubectl.KubectlService;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * 原生接口 Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "Kube API")
@RequestMapping("/kubectl")
@RestController
public class KubectlController extends AppManagerBaseController {

    @Autowired
    private KubectlService kubectlService;

    @GetMapping("namespaces")
    public TeslaBaseResult namespaces(@ModelAttribute KubectlListNamespaceReq req, OAuth2Authentication auth) {
        return buildSucceedResult(kubectlService.listNamespace(req, getOperator(auth)));
    }

    @PostMapping("apply")
    public TeslaBaseResult apply(
            @RequestBody KubectlApplyReq request,
            BindingResult validator, OAuth2Authentication auth) {
        if (validator.hasErrors()) {
            return buildValidationResult(validator);
        }
        kubectlService.apply(request, getOperator(auth));
        return buildSucceedResult(null);
    }

    @DeleteMapping("clusters/{cluster}/namespaces/{namespace}/deployments/{deploymentName}")
    public TeslaBaseResult deleteDeployment(
            @PathVariable(value = "cluster") String cluster,
            @PathVariable(value = "namespace") String namespace,
            @PathVariable(value = "deploymentName") String deploymentName,
            OAuth2Authentication auth) {
        KubectlDeleteDeploymentReq request = KubectlDeleteDeploymentReq.builder()
                .clusterId(cluster)
                .namespaceId(namespace)
                .deploymentName(deploymentName)
                .build();
        kubectlService.deleteDeployment(request, getOperator(auth));
        return buildSucceedResult(null);
    }

    @DeleteMapping("clusters/{cluster}/namespaces/{namespace}/statefulsets/{statefulSetName}")
    public TeslaBaseResult deleteStatefulSet(
            @PathVariable(value = "cluster") String cluster,
            @PathVariable(value = "namespace") String namespace,
            @PathVariable(value = "statefulSetName") String statefulSetName,
            OAuth2Authentication auth) {
        KubectlDeleteStatefulSetReq request = KubectlDeleteStatefulSetReq.builder()
                .clusterId(cluster)
                .namespaceId(namespace)
                .statefulSetName(statefulSetName)
                .build();
        kubectlService.deleteStatefulSet(request, getOperator(auth));
        return buildSucceedResult(null);
    }

    @DeleteMapping("clusters/{cluster}/namespaces/{namespace}/jobs/{jobName}")
    public TeslaBaseResult deleteJob(
            @PathVariable(value = "cluster") String cluster,
            @PathVariable(value = "namespace") String namespace,
            @PathVariable(value = "jobName") String jobName,
            @ModelAttribute KubectlDeleteJobReq params,
            OAuth2Authentication auth) {
        KubectlDeleteJobReq request = KubectlDeleteJobReq.builder()
                .clusterId(cluster)
                .namespaceId(namespace)
                .jobName(jobName)
                .asPrefix(params.isAsPrefix())
                .build();
        kubectlService.deleteJob(request, getOperator(auth));
        return buildSucceedResult(null);
    }
}
