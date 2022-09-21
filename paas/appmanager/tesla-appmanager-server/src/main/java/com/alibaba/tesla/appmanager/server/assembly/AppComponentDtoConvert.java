package com.alibaba.tesla.appmanager.server.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.AppComponentDTO;
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
}
