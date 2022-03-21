package com.alibaba.sreworks.cmdb.domain.req.model;

import com.alibaba.sreworks.cmdb.common.constant.DWConstant;
import com.alibaba.sreworks.cmdb.common.type.ColumnType;
import com.alibaba.sreworks.cmdb.common.utils.Regex;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;

/**
 * 新增模型列
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/07/21 17:04
 */

@ApiModel(value="新增模型列")
public class ModelFieldCreateReq extends ModelFieldBaseReq {
    @Override
    public String getField() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(field), "模型列名不允许为空");
        Preconditions.checkArgument(Regex.checkDocumentByPattern(field, DWConstant.ENTITY_MODEL_FIELD_PATTERN), "模型列名不符合规范:" + DWConstant.ENTITY_MODEL_FIELD_REGEX);
        return field;
    }

    @Override
    public String getAlias() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(alias), "模型列别名不允许为空");
        return alias;
    }

    @Override
    public String getDim() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(dim), "存储列名不允许为空");
        return dim;
    }

    @Override
    public Boolean getBuildIn() {
        return false;
    }

    @Override
    public ColumnType getType() {
        return type;
    }

    @Override
    public Boolean getNullable() {
        return nullable;
    }
}
