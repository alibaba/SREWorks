package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.spring.util.SpringBeanUtil
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import com.fasterxml.jackson.databind.ObjectMapper
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder
import io.fabric8.kubernetes.api.model.OwnerReference
import io.fabric8.kubernetes.api.model.networking.v1.Ingress
import io.fabric8.kubernetes.api.model.networking.v1.IngressBuilder
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpec
import io.fabric8.kubernetes.api.model.networking.v1.IngressSpecBuilder
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
/**
 * Service Ingress
 *
 * @author jiongen.zje@alibaba-inc.com
 */
class TraitIngress implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitIngress.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "ingress.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 11

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
         - name: ingress.trait.sreworks.io/v1beta1
         runtime: post
         spec:
           host: xxxx.cn-beijing.alicontainer.com
           path: /
           serviceName: xxxx-frontend
           servicePort: 80
         */

        /**
         * 1. get metadata from workload
         */
        log.info("start execute ingress trait {}", request.getSpec().toJSONString())

        WorkloadResource workloadResource = request.getRef()
        String namespace = workloadResource.getMetadata().getNamespace()
        String ownerReference = request.getOwnerReference()
        String name = workloadResource.getMetadata().getName()
        String clusterId = request.getComponent().getClusterId()
        String host = request.getSpec().getString("host")
        String path = request.getSpec().getString("path") ? request.getSpec().getString("path") : "/"
        String serviceName = request.getSpec().getString("serviceName") ? request.getSpec().getString("serviceName") : name
        Integer servicePort = request.getSpec().getInteger("servicePort") ? request.getSpec().getInteger("servicePort") : 80
        String ingressName = request.getSpec().getString("name") ? request.getSpec().getString("name") : name

        /**
         * 2. generate ingress and apply to k8s cluster
         */
        log.info("start to apply ingress by trait {}", request.getSpec().toJSONString())
        applyIngress(clusterId, namespace, ingressName, host, path, serviceName, servicePort, ownerReference)

        return TraitExecuteRes.builder()
                .spec(request.getSpec())
                .build()
    }

    private static void applyIngress(
            String clusterId, String namespace,
            String ingressName, String host, String path, String serviceName, Integer servicePort,
            String ownerReference){


        /**
         * apiVersion: networking.k8s.io/v1
           kind: Ingress
           metadata:
             name: health
             namespace: sreworks
           spec:
             rules:
             - host: xxxx.cn-hangzhou.alicontainer.com
               http:
                 paths:
                 - backend:
                   service:
                     name: prod-health-health
                     port:
                       number: 80
                   path: /
                   pathType: ImplementationSpecific
         */


        KubernetesClientFactory clientFactory = SpringBeanUtil.getBean(KubernetesClientFactory.class);
        DefaultKubernetesClient client = clientFactory.get(clusterId);
        ObjectMapper mapper = new ObjectMapper();

        // 创建 IngressBuilder 对象
        IngressBuilder ingressBuilder = new IngressBuilder();

        // 创建 Metadata 对象
        ObjectMeta metadata
        if(ownerReference == null) {
            metadata = new ObjectMetaBuilder()
                    .withName(ingressName)
                    .withNamespace(namespace)
                    .build();
        }else{
            metadata = new ObjectMetaBuilder()
                    .withName(ingressName)
                    .withNamespace(namespace)
                    .withOwnerReferences(mapper.readValue(ownerReference, OwnerReference.class))
                    .build();
        }

        IngressSpec ingressSpec = new IngressSpecBuilder()
                .addNewRule()
                    .withHost(host)
                    .withNewHttp()
                        .addNewPath()
                            .withPath(path)
                            .withPathType("ImplementationSpecific")
                            .withNewBackend()
                                .withNewService()
                                    .withName(serviceName)
                                    .withNewPort()
                                        .withNumber(servicePort)
                                    .endPort()
                                .endService()
                            .endBackend()
                        .endPath()
                    .endHttp()
                .endRule()
                .build();

        // 构建 Ingress 对象
        Ingress ingress = ingressBuilder.withMetadata(metadata).withSpec(ingressSpec).build();

        Ingress current = client.network().v1().ingresses().inNamespace(namespace).withName(ingressName).get();
        if (current == null) {
            Ingress result = client.network().v1().ingresses().createOrReplace(ingress);
            log.info("cr yaml has created in kubernetes|cluster={}|namespace={}|ingressName={}" +
                    "result={}", clusterId, namespace, ingressName, JSONObject.toJSONString(result));
        }else{
            Ingress result = client.network().v1().ingresses().inNamespace(namespace).withName(ingressName).patch(ingress);
            log.info("cr yaml has patched in kubernetes|cluster={}|namespace={}|ingressName={}" +
                    "result={}", clusterId, namespace, ingressName, JSONObject.toJSONString(result));
        }

    }

}
