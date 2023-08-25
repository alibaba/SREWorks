package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Topology Spread Constraints Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitTopologySpreadConstraints implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitTopologySpreadConstraints.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "topology.spread.constraints.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 1

    /**
     * 固定 Key
     */
    private static final String KEY = "topologySpreadConstraints"

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        def spec = request.getSpec()
        def topologySpreadConstraints = spec.getJSONArray(KEY)
        if (topologySpreadConstraints == null || topologySpreadConstraints.size() == 0) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty topologySpreadConstraints trait spec")
        }

        JSONObject workloadSpec = (JSONObject) request.getRef().getSpec()
        String workloadMetaStr = JSONObject.toJSONString(request.getRef().getMetadata())

        // 适配 cloneset 及 advancedstatefulset 类型，最后是兼容历史的类新，直接置到 workload spec 顶部
        if (workloadSpec.get("cloneSet") != null) {
            JSONObject cloneSetSpec = workloadSpec
                    .getJSONObject("cloneSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            if (cloneSetSpec.get(KEY) == null) {
                cloneSetSpec.put(KEY, topologySpreadConstraints)
                log.info("topologySpreadConstraints {} has applied to workload {}|kind=cloneSet|append=false",
                        topologySpreadConstraints.toJSONString(), workloadMetaStr)
            } else {
                cloneSetSpec.getJSONArray(KEY).addAll(topologySpreadConstraints)
                log.info("topologySpreadConstraints {} has applied to workload {}|kind=cloneSet|append=true",
                        topologySpreadConstraints.toJSONString(), workloadMetaStr)
            }
        } else if (workloadSpec.get("advancedStatefulSet") != null) {
            JSONObject advancedStatefulSetSpec = workloadSpec
                    .getJSONObject("advancedStatefulSet")
                    .getJSONObject("template")
                    .getJSONObject("spec")
            if (advancedStatefulSetSpec.get(KEY) == null) {
                advancedStatefulSetSpec.put(KEY, topologySpreadConstraints)
                log.info("topologySpreadConstraints {} has applied to workload {}|kind=advancedStatefulSet|append=false",
                        topologySpreadConstraints.toJSONString(), workloadMetaStr)
            } else {
                advancedStatefulSetSpec.getJSONArray(KEY).addAll(topologySpreadConstraints)
                log.info("topologySpreadConstraints {} has applied to workload {}|kind=advancedStatefulSet|append=true",
                        topologySpreadConstraints.toJSONString(), workloadMetaStr)
            }
        } else {
            workloadSpec.put(KEY, topologySpreadConstraints)
            log.info("topologySpreadConstraints {} has applied to workload {}|kind=compatible|append=false",
                    topologySpreadConstraints.toJSONString(), workloadMetaStr)
        }
        return TraitExecuteRes.builder()
                .spec(spec)
                .build()
    }
}
