package com.alibaba.tesla.appmanager.trait.plugin;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource;
import com.alibaba.tesla.appmanager.domain.schema.TraitDefinition;
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory;
import com.alibaba.tesla.appmanager.spring.util.SpringBeanUtil;
import com.alibaba.tesla.appmanager.trait.BaseTrait;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.networking.v1beta1.Ingress;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

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
        KubernetesClientFactory clientFactory = SpringBeanUtil.getBean(KubernetesClientFactory.class);
        WorkloadResource workloadResource = getWorkloadRef();
        String name = workloadResource.getMetadata().getName();
        String namespace = workloadResource.getMetadata().getNamespace();
        JSONObject spec = getSpec();
        String clusterId = spec.getString("cluster");
        if (StringUtils.isEmpty(clusterId)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "empty cluster in multiple cluster ingress trait spec");
        }
        JSONObject cr = generateIngress(namespace, name, spec.getJSONObject(clusterId), spec);

        // 获取指定 cluster 的 kubernetes client
        DefaultKubernetesClient client = clientFactory.get(clusterId);

        // 应用到集群
        try {
            Resource<Ingress> resource = client.network().v1beta1().ingresses()
                    .load(new ByteArrayInputStream(cr.toJSONString().getBytes(StandardCharsets.UTF_8)));
            Ingress current = client.network().v1beta1().ingresses().inNamespace(namespace).withName(name).get();
            // 如果存在的话，先删除再创建
            if (current != null) {
                client.network().v1beta1().ingresses().inNamespace(namespace).withName(name).delete();
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {}
            Ingress result = resource.create();
            log.info("cr yaml has created in kubernetes|cluster={}|namespace={}|name={}|cr={}" +
                            "result={}", clusterId, namespace, name, cr.toJSONString(),
                    JSONObject.toJSONString(result));
        } catch (Exception e) {
            String errorMessage = String.format("apply cr yaml to kubernetes failed|cluster=%s|namespace=%s|" +
                            "exception=%s|cr=%s", clusterId, namespace, ExceptionUtils.getStackTrace(e),
                    cr.toJSONString());
            log.error(errorMessage);
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage);
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
