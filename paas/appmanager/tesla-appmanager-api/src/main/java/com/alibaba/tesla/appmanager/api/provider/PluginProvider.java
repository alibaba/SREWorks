package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginEnableReq;
import org.springframework.web.multipart.MultipartFile;

/**
 * Plugin Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface PluginProvider {

    /**
     * 获取插件列表
     *
     * @param request 查询插件列表请求
     * @return 插件列表
     */
    Pagination<PluginDefinitionDTO> list(PluginQueryReq request);

    /**
     * 启用指定插件
     *
     * @param request 插件启用请求
     * @return 开启后的 PluginDefinition 对象
     */
    PluginDefinitionDTO enable(PluginEnableReq request);

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDTO
     */
    PluginDefinitionDTO upload(MultipartFile file, boolean force);
}
