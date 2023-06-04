package com.alibaba.tesla.appmanager.server.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.AppVersionDTO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppVersionDO;
import org.springframework.stereotype.Component;

/**
 * 应用版本 DTO 转换器
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Component
public class AppVersionDtoConvert extends BaseDtoConvert<AppVersionDTO, AppVersionDO> {

    public AppVersionDtoConvert() {
        super(AppVersionDTO.class, AppVersionDO.class);
    }
}
