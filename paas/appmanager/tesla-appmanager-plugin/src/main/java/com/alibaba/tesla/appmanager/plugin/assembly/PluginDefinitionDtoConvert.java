package com.alibaba.tesla.appmanager.plugin.assembly;

import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.domain.dto.PluginDefinitionDTO;
import com.alibaba.tesla.appmanager.plugin.repository.domain.PluginDefinitionDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PluginDefinitionDtoConvert extends BaseDtoConvert<PluginDefinitionDTO, PluginDefinitionDO> {

    public PluginDefinitionDtoConvert() {
        super(PluginDefinitionDTO.class, PluginDefinitionDO.class);
    }
}