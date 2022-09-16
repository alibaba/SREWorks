package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.ConditionUtil
import com.alibaba.tesla.appmanager.domain.req.rtcomponentinstance.RtComponentInstanceGetStatusReq
import com.alibaba.tesla.appmanager.domain.res.rtcomponentinstance.RtComponentInstanceGetStatusRes
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentCustomStatusHandler
import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.PathNotFoundException
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 自定义状态获取脚本: 通用资源
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class CustomStatusGenericResource implements ComponentCustomStatusHandler {

    private static final String OPERATOR_EXIST = "exist"
    private static final String OPERATOR_EQUAL = "equal"

    private static final Logger log = LoggerFactory.getLogger(CustomStatusGenericResource.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.CUSTOM_STATUS.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = "generic-resource/v1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 8

    @Override
    RtComponentInstanceGetStatusRes getStatus(
            RtComponentInstanceGetStatusReq request, DefaultKubernetesClient client, JSONObject options) {
        def logSuffix = String.format("clusterId=%s|namespaceId=%s|stageId=%s|appId=%s|componentType=%s|" +
                "componentName=%s|version=%s", request.getClusterId(), request.getNamespaceId(), request.getStageId(),
                request.getAppId(), request.getComponentType(), request.getComponentName(), request.getVersion())
        def endStatus = options.getString("endStatus")
        if (StringUtils.isEmpty(endStatus)) {
            endStatus = ComponentInstanceStatusEnum.COMPLETED.toString()
        }
        def resources = options.getJSONArray("resources")
        if (resources == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find resources in options")
        }

        def data = new JSONObject()
        for (def resource : resources.toJavaList(JSONObject.class)) {
            def definition = resource.getJSONObject("definition")
            if (definition == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find definition in resource")
            }
            def context = new CustomResourceDefinitionContext.Builder()
                    .withName(definition.getString("name"))
                    .withGroup(definition.getString("group"))
                    .withVersion(definition.getString("version"))
                    .withPlural(definition.getString("plural"))
                    .withScope(definition.getString("scope"))
                    .build()
            if (StringUtils.isAnyEmpty(context.getName(), context.getGroup(), context.getVersion(),
                    context.getPlural(), context.getScope())) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid definition in resource|resource=%s", JSONObject.toJSONString(resource)))
            }
            def namespace = resource.getString("namespace")
            def name = resource.getString("name")
            def alias = resource.getString("alias")
            def conditions = resource.getJSONArray("conditions")
            if (StringUtils.isAnyEmpty(namespace, name, alias) || conditions == null || conditions.size() == 0) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid resource configuration in options|resource=%s",
                                JSONObject.toJSONString(resource)))
            }
            for (def condition : conditions.toJavaList(JSONObject.class)) {
                def path = condition.getString("path")
                def operator = condition.getString("operator")
                def value = condition.get("value")
                if (StringUtils.isAnyEmpty(path, operator)) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("invalid condition in resources|condition=%s",
                                    JSONObject.toJSONString(condition)))
                }
                def obj = client.customResource(context).inNamespace(namespace).withName(name).get()
                if (obj == null) {
                    return RtComponentInstanceGetStatusRes.builder()
                            .status(ComponentInstanceStatusEnum.WARNING.toString())
                            .conditions(ConditionUtil.singleCondition("CheckResourceExists", "False",
                                    String.format("cannot find resource %s in namespace %s", name, namespace), ""))
                            .build()
                }
                DocumentContext objContext = JsonPath.parse(JSONObject.toJSONString(obj))
                def objValue
                try {
                    objValue = objContext.read(DefaultConstant.JSONPATH_PREFIX + path)
                } catch (PathNotFoundException ignored) {
                    objValue = null
                } catch (Exception e) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("read by jsonpath failed|obj=%s|path=%s|exception=%s",
                                    JSONObject.toJSONString(obj), path, ExceptionUtils.getStackTrace(e)))
                }
                switch (operator) {
                    case OPERATOR_EQUAL:
                        if (objValue == value) {
                            data.put(alias, removeUselessResourceFields(obj))
                            log.info("script=CustomStatusGenericResource|message=object compare equal|path={}|" +
                                    "operator={}|value={}|namespace={}|name={}|{}", path, operator, value,
                                    namespace, name, logSuffix)
                        } else {
                            log.info("script=CustomStatusGenericResource|message=object compare failed|path={}|" +
                                    "operator={}|value={}|objValue={}|namespace={}|name={}|{}", path, operator,
                                    value, objValue, namespace, name, logSuffix)
                            return RtComponentInstanceGetStatusRes.builder()
                                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                                    .conditions(ConditionUtil.singleCondition("CheckResourceEqual", "False",
                                            String.format("object compare failed|path=%s|operator=%s|value=%s|" +
                                                    "objValue=%s|namespace=%s|name=%s|%s", path, operator, value,
                                                    objValue, namespace, name, logSuffix), ""))
                                    .build()
                        }
                        break
                    case OPERATOR_EXIST:
                        if (objValue != null) {
                            data.put(alias, removeUselessResourceFields(obj))
                            log.info("script=CustomStatusGenericResource|message=object exist|path={}|operator={}|" +
                                    "objValue={}|namespace={}|name={}|{}", path, operator, objValue, namespace,
                                    name, logSuffix)
                        } else {
                            log.info("script=CustomStatusGenericResource|message=object not exist|path={}|" +
                                    "operator={}|objValue={}|namespace={}|name={}|{}",
                                    path, operator, objValue, namespace, name, logSuffix)
                            return RtComponentInstanceGetStatusRes.builder()
                                    .status(ComponentInstanceStatusEnum.WARNING.toString())
                                    .conditions(ConditionUtil.singleCondition("CheckResourceExist", "False",
                                            String.format("object not exist|path=%s|operator=%s|namespace=%s|" +
                                                    "name=%s|%s", path, operator, namespace, name, logSuffix), ""))
                                    .build()
                        }
                        break
                    default:
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS, "unsupported operator " + operator)
                }
            }
        }
        log.info("script=CustomStatusGenericResource|message=all resources compare finished|{}|data={}",
                logSuffix, JSONObject.toJSONString(data))
        return RtComponentInstanceGetStatusRes.builder()
                .status(endStatus)
                .conditions(ConditionUtil.dataOutputCondition(data))
                .build()
    }

    private static JSONObject removeUselessResourceFields(Object data) {
        def target = JSONObject.parseObject(JSONObject.toJSONString(data))
        target.remove("metadata")
        return target
    }
}