package com.alibaba.tesla.appmanager.trait.plugin;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.RequestUtil;
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource;
import com.alibaba.tesla.appmanager.domain.schema.TraitDefinition;
import com.alibaba.tesla.appmanager.trait.BaseTrait;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * 多集群 Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
public class MultipleClusterIngressTrait extends BaseTrait {

    public MultipleClusterIngressTrait(String name, TraitDefinition traitDefinition, JSONObject spec, WorkloadResource ref) {
        super(name, traitDefinition, spec, ref);
    }

    @Override
    public void execute() {
        WorkloadResource workloadResource = getWorkloadRef();
        String name = workloadResource.getMetadata().getName();
        String namespace = workloadResource.getMetadata().getNamespace();
        JSONObject spec = getSpec();
        String cluster = spec.getString("cluster");
        if (StringUtils.isEmpty(cluster)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "empty cluster in multiple cluster ingress trait spec");
        }
        JSONObject serviceSpec = generateIngress(namespace, name, spec.getJSONObject(cluster), spec);
        String content = JSONObject.toJSONString(ImmutableMap.of("yaml_content", serviceSpec));
        JSONObject params = new JSONObject();
        params.put("overwrite", "true");
        try {
            String ret = RequestUtil.post("http://abm-operator/kube/apply", params, content, new JSONObject());
            log.info("apply ingress request has sent to abm-operator|content={}|response={}", content, ret);
            JSONObject retJson = JSONObject.parseObject(ret);
            if (!"Ingress".equals(retJson.getJSONObject("data").getString("kind"))) {
                throw new AppException(AppErrorCode.DEPLOY_ERROR,
                        String.format("cannot apply cr to abm-operator|content=%s|ret=%s", content, ret));
            }
        } catch (Exception e) {
            throw new AppException(AppErrorCode.DEPLOY_ERROR,
                    String.format("cannot parse response from abm-operator|content=%s|exception=%s",
                            content, ExceptionUtils.getStackTrace(e)));
        }
    }

    /**
     * 创建 ingress JSON spec
     *
     * @param namespace   命名空间
     * @param name        标识名称
     * @param spec        Spec 定义
     * @param clusterSpec 指定 Cluster 级别的定义
     * @return JSONObject
     */
    private JSONObject generateIngress(String namespace, String name, JSONObject clusterSpec, JSONObject spec) {
        String ingressName = name;
        if (!StringUtils.isEmpty(clusterSpec.getString("ingressName"))) {
            ingressName = clusterSpec.getString("ingressName");
        }
        String serviceStr = JSONObject.toJSONString(ImmutableMap.of(
                "apiVersion", "extensions/v1beta1",
                "kind", "Ingress",
                "metadata", ImmutableMap.of(
                        "namespace", namespace,
                        "name", ingressName,
                        "labels", ImmutableMap.of(
                                "name", ingressName
                        ),
                        "annotations", ImmutableMap.of(
                                "kubernetes.io/ingress.class", "acs-ingress",
                                "nginx.ingress.kubernetes.io/enable-cors", "true",
                                "nginx.ingress.kubernetes.io/proxy-body-size", "4096m"
                        )
                )
        ));
        JSONObject service = JSONObject.parseObject(serviceStr);
        service.put("spec", clusterSpec);
        JSONObject annotations = spec.getJSONObject("annotations");
        if (annotations != null) {
            service.getJSONObject("metadata").getJSONObject("annotations").putAll(annotations);
        }
        return service;
    }
}
