package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.JSONArray
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.domain.req.componentpackage.BuildComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.res.componentpackage.LaunchBuildComponentHandlerRes
import com.alibaba.tesla.appmanager.server.assembly.ComponentPackageTaskDtoConvert
import com.alibaba.tesla.appmanager.server.service.componentpackage.ComponentPackageBuilderExecutorManager
import com.alibaba.tesla.appmanager.server.service.componentpackage.handler.BuildComponentHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
/**
 * 默认构建 MicroService Groovy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class MicroServiceV1Beta1ComponentBuildHandler implements BuildComponentHandler {

    private static final Logger log = LoggerFactory.getLogger(MicroServiceV1Beta1ComponentBuildHandler.class)

    /**
     * 当前脚本类型 (ComponentKindEnum)
     */
    public static final String KIND = DynamicScriptKindEnum.COMPONENT_BUILD.toString()

    /**
     * 当前脚本名称 (指定 SCRIPT_KIND 下唯一)
     */
    public static final String NAME = "microservice.component.sreworks.io/v1beta1"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 16

    private static final String TEMPLATE_MICROSERVICE_FILENAME = "default_microservice_%s.tpl"

    private static final String DEFAULT_MICROSERVICE_TYPE = "Deployment"


    @Autowired
    private ComponentPackageBuilderExecutorManager componentPackageBuilderExecutorManager

    @Autowired
    private ComponentPackageTaskDtoConvert componentPackageTaskDtoConvert

    /**
     * 构建一个实体 Component Package
     *
     * @param request ComponentPackage 创建任务对象
     * @return 实体包信息
     */
    @Override
    LaunchBuildComponentHandlerRes launch(BuildComponentHandlerReq request) {
        def componentType = request.getComponentType()
        def componentName = request.getComponentName()
        def version = request.getVersion()
        def appId = request.getAppId()
        def options = request.getOptions()
        def taskDO = componentPackageTaskDtoConvert.from(request.getTaskDTO())
        def sourceType = request.getOptions().getString("sourceType")


        // 构建日志内容输出
        def logContent = new StringBuilder()

        // 选择源
        if("empty" == sourceType){
            // 无来源
            log.info("sourceType: empty")
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "sourceType: empty not support", e)
        }else if("git" == sourceType || "team-git" == sourceType){
            // git代码仓库
            log.info("sourceType: {}", sourceType)

            logContent.append(String.format("git repo clone in kankio build service|repo=%s\n", options.getString("repo")))

        }else{
            def errorMessage = String.format("no sourceType found|options=%s\n", JSONObject.toJSONString(options))
            logContent.append(errorMessage)
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
        }

        // 将当前options重新制作成适合老格式的进行兼容
        log.info("taskDO getPackageOptions={}", taskDO.getPackageOptions())
        log.info("request options={}", options)
        def containers = options.getJSONArray("containers")
        def compatibleOptions = new JSONObject()
        def compatibleOptionsContainers = new JSONArray()
        compatibleOptions.put("containers", compatibleOptionsContainers)
        compatibleOptions.put("initContainers", new JSONArray())
        compatibleOptions.put("env", new JSONArray())
        if(containers){
            for (JSONObject container : containers) {
                def compatibleOptionsContainer = new JSONObject()
                def buildOptions = new JSONObject()
                buildOptions.put("repo", options.getString("repo"))
                buildOptions.put("imagePush", true)
                buildOptions.put("branch", options.getString("branch"))
                buildOptions.put("repoPath", options.getString("repoPath"))
                buildOptions.put("ciAccount", options.getString("ciAccount"))
                buildOptions.put("ciToken", options.getString("ciToken"))
                buildOptions.put("dockerfileTemplate", container.getString("dockerfile"))
                buildOptions.put("imagePushRegistry", container.getString("imagePushRegistry"))
                compatibleOptionsContainer.put("name", container.getString("name"))
                compatibleOptionsContainer.put("build", buildOptions)
                compatibleOptionsContainers.add(compatibleOptionsContainer)
            }
        }
        log.info("compatibleOptions={}", compatibleOptions.toJSONString())

        taskDO.setPackageOptions(compatibleOptions.toJSONString())

        // 构建方案
        componentPackageBuilderExecutorManager.getInstance("K8S_MICROSERVICE").exportComponentPackage(taskDO)

    }


}
