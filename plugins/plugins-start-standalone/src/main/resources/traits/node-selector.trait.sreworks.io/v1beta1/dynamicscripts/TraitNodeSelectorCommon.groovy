package dynamicscripts

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
/**
 * Node Selector
 *
 * @author jiongen.zje@alibaba-inc.com
 */
class TraitNodeSelectorCommon implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitNodeSelectorCommon.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "node-selector.trait.sreworks.io/v1beta1"

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
         - name: node-selector.trait.sreworks.io/v1beta1
           runtime: post
           spec:
             nodeSelector:
               a: b
               c: d
          - name: node-selector.trait.sreworks.io/v1beta1
           runtime: post
           spec:
             nodeSelector:
             - key: a
               value: b
             - key: c
               value: d
         */

        /**
         * 1. get metadata from workload
         */
        log.info("start execute node-selector trait {}", request.getSpec().toJSONString())
        def spec = request.getSpec()

        // nodeSelector 支持为字典或数组
        // 数组
        // - key: xxx
        //   value: yyy
        // 字段
        // key-aa: value-bbb
        Map<String, String> selector;
        Object raw = request.getSpec().get("nodeSelector")
        if (raw instanceof JSONObject){
            selector = request.getSpec().getJSONObject("nodeSelector").getInnerMap()
        }else if (raw instanceof JSONArray){
            selector = new HashMap<>()
            JSONArray selectorArray = request.getSpec().getJSONArray("nodeSelector")
            for (int i = 0; i < selectorArray.size(); i++) {
                String key = selectorArray.getJSONObject(i).getString("key")
                String value = selectorArray.getJSONObject(i).getString("value")
                selector.put(key, value)
            }
        }else{
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    "the nodeSelector is not a JSONObject or a JSONArray")
        }

        JSONObject nodeSelector = new JSONObject(selector)
        JSONObject workloadSpec = (JSONObject) request.getRef().getSpec()

        // 适配 cloneset 及 advancedstatefulset 类型，最后是兼容历史的类新，直接置到 workload spec 顶部
        if (workloadSpec.get("cloneSet") != null) {
            JSONObject cloneSetSpec = workloadSpec
                    .getJSONObject("cloneSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            if (cloneSetSpec.get("nodeSelector") == null) {
                cloneSetSpec.put("nodeSelector", nodeSelector)
                log.info("nodeSelector {} has applied to workload {}|kind=cloneSet|append=false",
                        nodeSelector.toJSONString(), JSONObject.toJSONString(request.getRef().getMetadata()))
            } else {
                cloneSetSpec.getJSONObject("nodeSelector").putAll(nodeSelector)
                log.info("nodeSelector {} has applied to workload {}|kind=cloneSet|append=true",
                        nodeSelector.toJSONString(), JSONObject.toJSONString(request.getRef().getMetadata()))
            }
        } else if (workloadSpec.get("advancedStatefulSet") != null) {
            JSONObject advancedStatefulSetSpec = workloadSpec
                    .getJSONObject("advancedStatefulSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            if (advancedStatefulSetSpec.get("nodeSelector") == null) {
                advancedStatefulSetSpec.put("nodeSelector", nodeSelector)
                log.info("nodeSelector {} has applied to workload {}|kind=advancedStatefulSet|append=false",
                        nodeSelector.toJSONString(), JSONObject.toJSONString(request.getRef().getMetadata()))
            } else {
                advancedStatefulSetSpec.getJSONObject("nodeSelector").putAll(nodeSelector)
                log.info("nodeSelector {} has applied to workload {}|kind=advancedStatefulSet|append=true",
                        nodeSelector.toJSONString(), JSONObject.toJSONString(request.getRef().getMetadata()))
            }
        } else {
            workloadSpec.put("nodeSelector", nodeSelector)
            log.info("nodeSelector {} has applied to workload {}|kind=compatible|append=false",
                    nodeSelector.toJSONString(), JSONObject.toJSONString(request.getRef().getMetadata()))
        }
        return TraitExecuteRes.builder()
                .spec(spec)
                .build()
    }

}
