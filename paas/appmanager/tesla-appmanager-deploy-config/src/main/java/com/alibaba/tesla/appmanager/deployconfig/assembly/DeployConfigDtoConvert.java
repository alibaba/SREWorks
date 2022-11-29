package com.alibaba.tesla.appmanager.deployconfig.assembly;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.assembly.BaseDtoConvert;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Deploy Config DTO Converter
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Service
public class DeployConfigDtoConvert extends BaseDtoConvert<DeployConfigDTO, DeployConfigDO> {

    public DeployConfigDtoConvert() {
        super(DeployConfigDTO.class, DeployConfigDO.class);
    }

    public DeployConfigDTO to(DeployConfigDO DeployConfigDO) {
        if (DeployConfigDO == null) {
            return null;
        }

        DeployConfigDTO result = new DeployConfigDTO();
        ClassUtil.copy(DeployConfigDO, result);
        if (StringUtils.isNotEmpty(result.getConfig())) {
            String restStr = result.getConfig();
            if (restStr.startsWith("!!map")) {
                restStr = restStr.substring("!!map".length());
            }
            restStr = restStr.trim();
            if (restStr.startsWith("-")) {
                List<JSONObject> list = SchemaUtil.toSchemaList(JSONObject.class, result.getConfig());
                result.setConfigJson(JSONArray.parseArray(JSONArray.toJSONString(list)));
            } else {
                result.setConfigJson(SchemaUtil.toSchema(JSONObject.class, result.getConfig()));
            }
        }
        return result;
    }
}
