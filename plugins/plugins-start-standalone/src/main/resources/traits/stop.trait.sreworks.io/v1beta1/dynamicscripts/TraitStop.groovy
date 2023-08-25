package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.server.lib.validator.AppManagerValidateUtil
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import com.alibaba.tesla.dag.common.BeanUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.validation.constraints.NotNull
import java.util.concurrent.TimeUnit
/**
 * Stop Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitStop implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitStop.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "stop.trait.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 2

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        def validateUtil = BeanUtil.getBean(AppManagerValidateUtil.class);

        def specJsonObject = request.getSpec();
        def spec = JSONObject.parseObject(specJsonObject.toJSONString(), Spec.class);
        validateUtil.validate(spec);
        log.info("start stop")

        try {
            TimeUnit.SECONDS.sleep(spec.getSeconds());
        }catch (Exception e) {

        }

        log.info("stop finished")

        return TraitExecuteRes.builder()
                .spec(request.getSpec())
                .build()
    }

    public static class Spec {


        @NotNull
        private Integer seconds;

        Integer getSeconds() {
            return seconds
        }

        void setSeconds(Integer seconds) {
            this.seconds = seconds
        }
    }
}
