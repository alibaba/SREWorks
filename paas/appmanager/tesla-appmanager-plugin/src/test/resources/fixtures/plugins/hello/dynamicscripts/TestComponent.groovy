package dynamicscripts

import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentHandler

class AbmHelmComponentHandler implements ComponentHandler {

    /**
     * Handler 元信息
     */
    public static final String KIND = "COMPONENT"
    public static final String NAME = "TEST_COMPONENT"
    public static final Integer REVISION = 0

    /**
     * 获取 `COMPONENT_BUILD` 类型下的映射名称
     *
     * @return 示例：`AbmChartDefault`
     */
    @Override
    String buildScriptName() {
        return "AbmHelmComponentDefault"
    }

    /**
     * 获取 `COMPONENT_DEPLOY` 类型下的映射名称
     *
     * @return 示例：`JobDefault` / `HelmDefault`
     */
    @Override
    String deployScriptName() {
        return "AbmHelmComponentDefault"
    }

    /**
     * 获取 `COMPONENT_DESTROY` 类型下的映射名称
     *
     * @return 示例：`HelmDefault`
     */
    @Override
    String destroyName() {
        return "AbmHelmComponentDefault"
    }

    /**
     * 获取状态监听类型
     *
     * @return 返回 `KUBERNETES_INFORMER` 或 `CRON`
     */
    @Override
    String watchKind() {
        return "CRON"
    }

    /**
     * 如果 `watchKind` 返回 `KUBERNETES_INFORMER`，则对应 `COMPONENT_WATCH_KUBERNETES_INFORMER` 类型下的映射名称
     * <p>
     * 如果 `watchKind` 返回 `CRON`，则对应 `COMPONENT_WATCH_CRON` 类型下的映射名称
     *
     * @return 返回对应类型下的映射名称
     */
    @Override
    String watchScriptName() {
        return "AbmHelmComponentCron"
    }
}
