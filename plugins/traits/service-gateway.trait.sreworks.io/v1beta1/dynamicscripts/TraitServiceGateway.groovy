package dynamicscripts

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.spring.util.SpringBeanUtil
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableMap
import io.fabric8.kubernetes.api.model.OwnerReference
import io.fabric8.kubernetes.api.model.Service
import io.fabric8.kubernetes.api.model.ServiceBuilder
import io.fabric8.kubernetes.api.model.ServiceSpec
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.dsl.ServiceResource
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.nio.charset.StandardCharsets
/**
 * Service Trait
 *
 * @author jiongen.zje@alibaba-inc.com
 */
class TraitServiceGateway implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitServiceGateway.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "service-gateway.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 1

    @Autowired
    private KubernetesClientFactory clientFactory

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {

        /**
         - name: service-gateway.trait.sreworks.io/v1beta1
           runtime: post
           spec:
             protocol: TCP
             port: 7001
             authEnabled: true
             routePath: /sreworks/**
             routeOrder: 5000
         */

        /**
         * 1. get metadata from workload
         */
        log.info("start execute service-gateway trait {}", request.getSpec().toJSONString())

        WorkloadResource workloadResource = request.getRef()
        String namespace = workloadResource.getMetadata().getNamespace()
        String ownerReference = request.getOwnerReference()
        String name = workloadResource.getMetadata().getName()
        String clusterId = request.getComponent().getClusterId()
        JSONObject annotations = request.getSpec().getJSONObject("annotations")
        JSONObject labels = request.getSpec().getJSONObject("labels")
        request.getSpec().remove("annotations")
        request.getSpec().remove("labels")
        annotations = annotations == null ? new JSONObject() : annotations
        labels = labels == null ? new JSONObject() : labels


        /**
         * 2. generate service and apply to k8s cluster
         */
        log.info("start to apply service in service-gateway trait {}", request.getSpec().toJSONString())
        JSONObject spec = ImmutableMap.of(
                "ports", ImmutableList.of(
                ImmutableMap.of(
                        "port",80,
                        "protocol", request.getSpec().getString("protocol"),
                        "targetPort", request.getSpec().getString("port")
                )
        )
        ) as JSONObject

        JSONObject serviceCr = generateService(namespace, name, labels, annotations, spec, ownerReference)
        applyService(clusterId, serviceCr, namespace, name, labels, annotations, ownerReference)

        /**
         * 3. apply route to gateway
         */

        return TraitExecuteRes.builder()
                .spec(request.getSpec())
                .build()
    }

    /**
     * 应用 Service 到集群中
     *
     * @param clusterId      集群 ID
     * @param cr             CR
     * @param namespace      Namespace
     * @param name           Name
     * @param labels         Labels
     * @param annotations    Annotations
     * @param ownerReference Owner Reference
     */
    private void applyService(
            String clusterId, JSONObject cr, String namespace, String name, JSONObject labels,
            JSONObject annotations, String ownerReference) {
        KubernetesClientFactory clientFactory = SpringBeanUtil.getBean(KubernetesClientFactory.class);
        DefaultKubernetesClient client = clientFactory.get(clusterId);
        // 应用到集群
        try {
            ServiceResource<Service> resource = client.services()
                    .load(new ByteArrayInputStream(cr.toJSONString().getBytes(StandardCharsets.UTF_8)));
            log.info("prepare to apply service cr|cluster={}|namespace={}|name={}|cr={}",
                    clusterId, namespace, name, cr.toJSONString());
            try {
                Service current = client.services().inNamespace(namespace).withName(name).get();
                if (current == null) {
                    Service result = resource.create();
                    log.info("cr yaml has created in kubernetes|cluster={}|namespace={}|name={}|cr={}" +
                            "result={}", clusterId, namespace, name, cr.toJSONString(),
                            JSONObject.toJSONString(result));
                } else {
                    ObjectMapper mapper = new ObjectMapper();
                    ServiceSpec newSpec = mapper.readValue(cr.getJSONObject("spec").toJSONString(), ServiceSpec.class);
                    Integer healthCheckNodePort = current.getSpec().getHealthCheckNodePort();
                    if (healthCheckNodePort != null && healthCheckNodePort > 0) {
                        newSpec.setHealthCheckNodePort(healthCheckNodePort);
                    }
                    String clusterIp = current.getSpec().getClusterIP();
                    if (StringUtils.isNotEmpty(clusterIp)) {
                        newSpec.setClusterIP(clusterIp);
                    }
                    final JSONObject finalLabels = labels;
                    final JSONObject finalAnnotations = annotations;
                    Service result = client.services()
                            .inNamespace(namespace)
                            .withName(name)
                            .edit(s -> {
                                if (StringUtils.isNotEmpty(ownerReference)) {
                                    try {
                                        return new ServiceBuilder(s)
                                                .editMetadata()
                                                .withLabels(JSON.parseObject(finalLabels.toJSONString(), new TypeReference<Map<String, String>>() {
                                                }))
                                                .withAnnotations(JSON.parseObject(finalAnnotations.toJSONString(), new TypeReference<Map<String, String>>() {
                                                }))
                                                .withOwnerReferences(mapper.readValue(ownerReference, OwnerReference.class))
                                                .endMetadata()
                                                .withSpec(newSpec)
                                                .build();
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                } else {
                                    return new ServiceBuilder(s)
                                            .editMetadata()
                                            .withLabels(JSON.parseObject(finalLabels.toJSONString(), new TypeReference<Map<String, String>>() {
                                            }))
                                            .withAnnotations(JSON.parseObject(finalAnnotations.toJSONString(), new TypeReference<Map<String, String>>() {
                                            }))
                                            .endMetadata()
                                            .withSpec(newSpec)
                                            .build();
                                }
                            });
                    log.info("cr yaml has updated in kubernetes|cluster={}|namespace={}|name={}|labels={}|" +
                            "annotations={}|newSpec={}|result={}", clusterId, namespace, name,
                            JSONObject.toJSONString(labels), JSONObject.toJSONString(annotations),
                            JSONObject.toJSONString(newSpec), JSONObject.toJSONString(result));
                }
            } catch (KubernetesClientException e) {
                if (e.getCode() == 422) {
                    log.error("service apply failed, exception={}", ExceptionUtils.getStackTrace(e));
                } else {
                    throw e;
                }
            }
        } catch (Exception e) {
            String errorMessage = String.format("apply cr yaml to kubernetes failed|cluster=%s|namespace=%s|" +
                    "exception=%s|cr=%s", clusterId, namespace, ExceptionUtils.getStackTrace(e),
                    cr.toJSONString());
            log.error(errorMessage);
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage);
        }
    }

    /**
     * 创建服务 JSON spec
     *
     * @param namespace   命名空间
     * @param name        标识名称
     * @param labels      Labels
     * @param annotations Annotations
     * @param spec        spec 定义
     * @return JSONObject
     */
    private static JSONObject generateService(
            String namespace, String name, JSONObject labels, JSONObject annotations, JSONObject spec,
            String ownerReference) {
        String serviceStr = JSONObject.toJSONString(ImmutableMap.of(
                "apiVersion", "v1",
                "kind", "Service",
                "metadata", ImmutableMap.of(
                "namespace", namespace,
                "name", name,
                "labels", labels,
                "annotations", annotations
        ),
                "spec", ImmutableMap.of(
                "selector", ImmutableMap.of(
                "name", name
        )
        )
        ));
        JSONObject service = JSONObject.parseObject(serviceStr);
        if (StringUtils.isNotEmpty(ownerReference)) {
            service.getJSONObject("metadata").put("ownerReferences", new JSONArray());
            service.getJSONObject("metadata").getJSONArray("ownerReferences")
                    .add(JSONObject.parseObject(ownerReference));
        }
        service.getJSONObject("spec").putAll(spec);
        return service;
    }

}
