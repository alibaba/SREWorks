package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Trait Delete Sts Abnormal Pod
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitDeleteStsAbnormalPod implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitDeleteStsAbnormalPod.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "deleteStsAbnormalPod.trait.abm.io"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 0

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        JSONObject spec = request.getSpec()
        return TraitExecuteRes.builder()
                .spec(spec)
                .build()
    }
}
