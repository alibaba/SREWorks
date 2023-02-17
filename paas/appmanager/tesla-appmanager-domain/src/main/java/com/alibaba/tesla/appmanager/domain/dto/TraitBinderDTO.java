package com.alibaba.tesla.appmanager.domain.dto;

import com.alibaba.fastjson.JSONObject;

import com.alibaba.tesla.appmanager.common.util.SecurityUtil;
import lombok.Data;

/**
 * @author qianmo.zm@alibaba-inc.com
 * @date 2020/11/10.
 */
@Data
public class TraitBinderDTO {
    /**
     * 插件ID
     */
    private String name;

    /**
     *
     */
    private JSONObject spec;

    /**
     * 检查参数合法性
     */
    public void checkParameters() {
        SecurityUtil.checkInput(name);
    }
}
