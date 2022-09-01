package com.alibaba.tesla.appmanager.plugin.service;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.req.PluginQueryReq;
import com.alibaba.tesla.appmanager.domain.req.plugin.PluginEnableReq;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Plugin 服务接口
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface PluginService {

    /**
     * 获取插件列表
     *
     * @param request 查询插件列表请求
     * @return 插件列表
     */
    Pagination<PluginDefinitionDO> list(PluginQueryReq request);

    /**
     * 启用指定插件
     *
     * @param request 插件启用请求
     * @return 开启后的 PluginDefinition 对象
     */
    PluginDefinitionDO enable(PluginEnableReq request);

    /**
     * 上传插件 (默认不启用)
     *
     * @param file  API 上传文件
     * @param force 是否强制上传覆盖
     * @return PluginDefinitionDO
     */
    PluginDefinitionDO upload(MultipartFile file, boolean force) throws IOException;
}
