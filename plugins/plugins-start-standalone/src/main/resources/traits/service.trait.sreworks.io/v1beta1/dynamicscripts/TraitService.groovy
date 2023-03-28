package dynamicscripts

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.core.WorkloadResource
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.spring.util.SpringBeanUtil
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import io.fabric8.kubernetes.api.model.*
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
/**
 * NodePort Service
 *
 * @author jiongen.zje@alibaba-inc.com
 */
class TraitService implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitService.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "service.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 1

    @Autowired
    private KubernetesClientFactory clientFactory

    /**
     * NodePort 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {

        /**
            name: service.trait.sreworks.io/v1beta1
            runtime: post
            spec:
                backendPort: 8080
                selector:
                  app: my-app
         */

        /**
         * 1. get metadata from workload
         */
        log.info("start execute service trait {}", request.getSpec().toJSONString())

        WorkloadResource workloadResource = request.getRef()
        String namespace = workloadResource.getMetadata().getNamespace()
        String ownerReference = request.getOwnerReference()
        String name = workloadResource.getMetadata().getName()
        String serviceName = request.getSpec().getString("name") ? request.getSpec().getString("name"): name
        Integer backendPort = request.getSpec().getInteger("backendPort")
        String clusterId = request.getComponent().getClusterId()

        // selector 支持为字典或数组
        // 数组
        // - key: xxx
        //   value: yyy
        // 字段
        // key-aa: value-bbb
        Map<String, String> selector;
        Object raw = request.getSpec().get("selector")
        if (raw instanceof JSONObject){
            selector = request.getSpec().getJSONObject("selector").getInnerMap()
        }else if (raw instanceof JSONArray){
            selector = new HashMap<>()
            JSONArray selectorArray = request.getSpec().getJSONArray("selector")
            for (int i = 0; i < selectorArray.size(); i++) {
                String key = selectorArray.getJSONObject(i).getString("key");
                String value = selectorArray.getJSONObject(i).getString("value");
                selector.put(key, value);
            }
        }else{
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "the selector is not a JSONObject or a JSONArray")
        }

        /**
         * 2. generate service and apply to k8s cluster
         */
        log.info("start to apply  service by trait {}", request.getSpec().toJSONString())

        /**
            apiVersion: v1
            kind: Service
            metadata:
              name: my-service
            spec:
              clusterIP: None
              selector:
                app: my-app
              ports:
                - name: http
                  port: 80
                  targetPort: 8080
         */

        KubernetesClientFactory clientFactory = SpringBeanUtil.getBean(KubernetesClientFactory.class)
        DefaultKubernetesClient client = clientFactory.get(clusterId)

        // 创建 ServicePort 对象
        ServicePortBuilder servicePortBuilder = new ServicePortBuilder();
        servicePortBuilder.withName("http");
        servicePortBuilder.withPort(80);
        servicePortBuilder.withTargetPort(new IntOrString(backendPort));

        // 创建 ServiceSpec 对象
        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder();
        serviceSpecBuilder.withClusterIP("None");
        serviceSpecBuilder.withSelector(selector);
        serviceSpecBuilder.withPorts(servicePortBuilder.build());

        // 创建 Service 对象
        ServiceBuilder serviceBuilder = new ServiceBuilder();
        serviceBuilder.withNewMetadata()
                .withName(serviceName)
                .withNamespace(namespace)
        .endMetadata();
        serviceBuilder.withSpec(serviceSpecBuilder.build());

        Service service = serviceBuilder.build();

        Service current = client.services().inNamespace(namespace).withName(serviceName).get();
        if (current == null) {
            Service result = client.services().createOrReplace(service);
            log.info("cr yaml has created in kubernetes|cluster={}|namespace={}|ServiceName={}" +
                    "result={}", clusterId, namespace, serviceName, JSONObject.toJSONString(result));
        }else{
            Service result = client.services().inNamespace(namespace).withName(serviceName).patch(service);
            log.info("cr yaml has patched in kubernetes|cluster={}|namespace={}|ServiceName={}" +
                    "result={}", clusterId, namespace, serviceName, JSONObject.toJSONString(result));
        }

        return TraitExecuteRes.builder()
                .spec(request.getSpec())
                .build()
    }

}
