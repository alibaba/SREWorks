package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.domain.dto.AppVersionDTO;
import com.alibaba.tesla.appmanager.domain.req.appversion.AppVersionCreateReq;

import java.util.List;

/**
 * 应用关联版本 Provider
 *
 * @author jiongen.zje@alibaba-inc.com
 */
public interface AppVersionProvider {

    AppVersionDTO get(String appId, String version);

    /**
     * 创建应用版本
     *
     * @param request  创建请求
     * @param operator 操作人
     * @return 版本信息
     */
    AppVersionDTO create(AppVersionCreateReq request, String operator);


    List<AppVersionDTO> list(String appId);

    void delete(String appId, String version, String operator);


//
//    /**
//     * 更新应用下的关联 Component 绑定
//     *
//     * @param request  更新请求
//     * @param operator 操作人
//     * @return 绑定后的结果
//     */
//    AppComponentDTO update(AppComponentUpdateReq request, String operator);
//

//    void delete(AppComponentDeleteReq request, String operator);
//
//    /**
//     * 获取指定 appId 下的所有关联 Component 对象
//     *
//     * @param request  查询请求
//     * @param operator 操作人
//     * @return List of AppComponentDTO
//     */
//    List<AppComponentDTO> list(AppComponentQueryReq request, String operator);
}
