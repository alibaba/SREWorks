package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import io.fabric8.kubernetes.client.KubernetesClientException
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Trait Service Account Patch
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitServiceAccountPatch implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitServiceAccountPatch.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "service.account.patch.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 2

    @Autowired
    private KubernetesClientFactory kubernetesClientFactory

    /**
     * CRD Context
     */
    private static final CustomResourceDefinitionContext CRD_CONTEXT = new CustomResourceDefinitionContext.Builder()
            .withName("serviceaccounts")
            .withGroup("")
            .withVersion("v1")
            .withPlural("serviceaccounts")
            .withScope("Namespaced")
            .build()

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        def spec = request.getSpec()
        def kubeconfig = spec.getString("kubeconfig")
        def resource = spec.getJSONObject("resource")
        def metadata = resource.getJSONObject("metadata")
        def namespace = metadata.getString("namespace")
        def name = metadata.getString("name")
        if (StringUtils.isAnyEmpty(namespace, name)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "invalid namespace/name in service account patch trait")
        }
        if (StringUtils.isEmpty(kubeconfig)) {
            for (DeployAppSchema.SpecComponentScope scope : request.getComponent().getScopes()) {
                if (scope.getScopeRef().getKind() == "Kubeconfig") {
                    kubeconfig = scope.getScopeRef().getName()
                    break
                }
            }
            if (StringUtils.isEmpty(kubeconfig)) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        "cannot find kubeconfig in service account patch trait")
            }
        }
        def client = kubernetesClientFactory.getByKubeConfig(kubeconfig)

        // 不存在则新增，否则进行更新
        resource.put("apiVersion", "v1")
        resource.put("kind", "ServiceAccount")
        try {
            def currentCr = client.customResource(CRD_CONTEXT).get(namespace, name)
            if (currentCr == null || currentCr.size() == 0) {
                def result = client.customResource(CRD_CONTEXT).create(namespace, resource)
                log.info("service account has created in kubernetes|result={}", JSONObject.toJSONString(result))
            } else {
                def current = new JSONObject(currentCr)
                current.put("secrets", resource.getJSONArray("secrets"))
                current.put("imagePullSecrets", resource.getJSONArray("imagePullSecrets"))
                current.put("automountServiceAccountToken", resource.getBoolean("automountServiceAccountToken"))
                def result = client.customResource(CRD_CONTEXT).edit(namespace, name, current)
                log.info("service account has updated in kubernetes|result={}", JSONObject.toJSONString(result))
            }
        } catch (KubernetesClientException e) {
            if (e.getCode() == 404) {
                def result = client.customResource(CRD_CONTEXT).create(namespace, resource)
                log.info("service account has created in kubernetes|result={}", JSONObject.toJSONString(result))
            } else {
                throw e
            }
        }

        return TraitExecuteRes.builder()
                .spec(spec)
                .build()
    }
}
