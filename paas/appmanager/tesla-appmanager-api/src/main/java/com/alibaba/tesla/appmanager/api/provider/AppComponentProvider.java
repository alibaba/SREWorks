package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import com.alibaba.tesla.appmanager.domain.dto.AppComponentDTO;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentCreateReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentDeleteReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentQueryReq;
import com.alibaba.tesla.appmanager.domain.req.appcomponent.AppComponentUpdateReq;

import java.util.List;

/**
 * 应用关联组件 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface AppComponentProvider {

    /**
     * 获取指定应用下绑定了哪些组件及组件名称
     * @param appId 应用 ID
     * @param isolateNamespaceId 隔离 Namespace ID
     * @param isolateStageId 隔离 Stage ID
     * @return List of AppComponentLocationContainer
     */
    List<AppComponentLocationContainer> getFullComponentRelations(
            String appId, String isolateNamespaceId, String isolateStageId);

    /**
     * 获取指定应用下的指定关联 Component 对象
     *
     * @param request  应用组件绑定查询请求
     * @param operator 操作人
     * @return AppComponentDTO
     */
    AppComponentDTO get(AppComponentQueryReq request, String operator);

    /**
     * 创建应用下的关联 Component 绑定
     *
     * @param request  创建请求
     * @param operator 操作人
     * @return 绑定后的结果
     */
    AppComponentDTO create(AppComponentCreateReq request, String operator);

    /**
     * 更新应用下的关联 Component 绑定
     *
     * @param request  更新请求
     * @param operator 操作人
     * @return 绑定后的结果
     */
    AppComponentDTO update(AppComponentUpdateReq request, String operator);

    /**
     * 删除指定应用下的指定关联 Component 对象
     *
     * @param request  应用组件绑定查询请求
     * @param operator 操作人
     * @return AppComponentDTO
     */
    void delete(AppComponentDeleteReq request, String operator);

    /**
     * 获取指定 appId 下的所有关联 Component 对象
     *
     * @param request  查询请求
     * @param operator 操作人
     * @return List of AppComponentDTO
     */
    List<AppComponentDTO> list(AppComponentQueryReq request, String operator);
}
