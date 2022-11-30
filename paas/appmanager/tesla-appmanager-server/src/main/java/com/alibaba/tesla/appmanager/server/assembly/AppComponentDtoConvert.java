package com.alibaba.tesla.appmanager.server.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.domain.container.DeployConfigTypeId;
import com.alibaba.tesla.appmanager.domain.dto.AppComponentDTO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppComponentDO;
import org.springframework.stereotype.Component;

/**
 * 应用绑定组件 转换器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Component
public class AppComponentDtoConvert extends BaseDtoConvert<AppComponentDTO, AppComponentDO> {

    public AppComponentDtoConvert() {
        super(AppComponentDTO.class, AppComponentDO.class);
    }

    public AppComponentDTO to(AppComponentDO appComponentDO, PluginDefinitionDO pluginDefinitionDO) {
        if (appComponentDO == null) {
            return null;
        }

        AppComponentDTO result = new AppComponentDTO();
        ClassUtil.copy(appComponentDO, result);
        result.setPluginVersion(pluginDefinitionDO.getPluginVersion());
        // 通过当前方法转换的记录，均为 compatible=false
        result.setCompatible(false);
        // 自动转换当前类型映射的 typeId
        DeployConfigTypeId typeId = new DeployConfigTypeId(result.getComponentType(), result.getComponentName());
        result.setTypeId(typeId.toString());
        return result;
    }
}
