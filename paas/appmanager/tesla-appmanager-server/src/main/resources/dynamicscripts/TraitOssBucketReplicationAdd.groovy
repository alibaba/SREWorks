package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.domain.req.trait.TraitExecuteReq
import com.alibaba.tesla.appmanager.domain.res.trait.TraitExecuteRes
import com.alibaba.tesla.appmanager.trait.service.handler.TraitHandler
import com.aliyun.oss.OSS
import com.aliyun.oss.OSSClientBuilder
import com.aliyun.oss.OSSException
import com.aliyun.oss.model.AddBucketReplicationRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Trait Oss Bucket Create Trait
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class TraitOssBucketReplicationAdd implements TraitHandler {

    private static final Logger log = LoggerFactory.getLogger(TraitOssBucketReplicationAdd.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.TRAIT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "ossBucketReplicationAdd.trait.abm.io"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 4

    /**
     * Trait 业务侧逻辑执行
     *
     * @param request Trait 输入参数
     * @return Trait 修改后的 Spec 定义
     */
    @Override
    TraitExecuteRes execute(TraitExecuteReq request) {
        def spec = request.getSpec()
        JSONObject params = spec.getJSONObject("params")
        String ep = params.getString("ep")
        String ak = params.getString("ak")
        String sk = params.getString("sk")
        String targetBucketLocation = params.getString("targetBucketLocation")
        OSS ossClient = new OSSClientBuilder().build(ep, ak, sk);
        String sourceName = params.getString("sourceName")
        String targetName = params.getString("targetName")

        try {
            AddBucketReplicationRequest addBucketReplicationRequest = new AddBucketReplicationRequest(sourceName);
            addBucketReplicationRequest.setTargetBucketName(targetName);
            addBucketReplicationRequest.setTargetBucketLocation(targetBucketLocation);
            addBucketReplicationRequest.setEnableHistoricalObjectReplication(true)
            ossClient.addBucketReplication(addBucketReplicationRequest)
        } catch (OSSException e) {
            if (e.getErrorCode() == "InvalidTargetBucket" || e.getErrorCode() == "BucketReplicationAlreadyExist") {
            } else {
                throw e
            }
        }
        return TraitExecuteRes.builder()
                .spec(spec)
                .build()
    }
}
