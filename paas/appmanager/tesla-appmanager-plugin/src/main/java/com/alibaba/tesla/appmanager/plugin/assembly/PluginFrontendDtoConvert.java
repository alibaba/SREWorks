package com.alibaba.tesla.appmanager.plugin.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.PluginFrontendDTO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginFrontendDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PluginFrontendDtoConvert extends BaseDtoConvert<PluginFrontendDTO, PluginFrontendDO> {

    public PluginFrontendDtoConvert() {
        super(PluginFrontendDTO.class, PluginFrontendDO.class);
    }
}