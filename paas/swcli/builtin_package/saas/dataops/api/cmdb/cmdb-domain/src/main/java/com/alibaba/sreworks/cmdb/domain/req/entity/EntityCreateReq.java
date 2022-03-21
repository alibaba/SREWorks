package com.alibaba.sreworks.cmdb.domain.req.entity;

import com.alibaba.sreworks.cmdb.common.constant.DWConstant;
import com.alibaba.sreworks.cmdb.common.utils.Regex;
import com.google.common.base.Preconditions;
import io.swagger.annotations.ApiModel;
import org.apache.commons.lang3.StringUtils;

/**
 * 创建实体元信息
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/07/21 17:04
 */

@ApiModel(value="创建实体元信息")
public class EntityCreateReq extends EntityBaseReq {
    @Override
    public String getName() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(name), "实体名称不允许为空");
        Preconditions.checkArgument(Regex.checkDocumentByPattern(name, DWConstant.ENTITY_MODEL_NAME_PATTERN), "实体名称不符合规范:" + DWConstant.ENTITY_MODEL_NAME_REGEX);
        return name;
    }

    @Override
    public String getAlias() {
        Preconditions.checkArgument(StringUtils.isNotEmpty(alias), "实体别名不允许为空");
        return alias;
    }

    @Override
    public Boolean getBuildIn() {
        return false;
    }

    @Override
    public String getLayer() {
        return DWConstant.ENTITY_DW_LAYER;
    }

    @Override
    public String getPartitionFormat() {
        partitionFormat = partitionFormat == null ? DWConstant.PARTITION_BY_DAY : partitionFormat;
        return partitionFormat;
    }

    @Override
    public Integer getLifecycle() {
        lifecycle = lifecycle == null ? DWConstant.DEFAULT_LIFE_CYCLE : lifecycle;
        Preconditions.checkArgument((lifecycle >= DWConstant.MIN_LIFE_CYCLE) && (lifecycle <= DWConstant.MAX_LIFE_CYCLE),
                String.format("生命周期参数非法,合理周期范围[%s, %s]", DWConstant.MIN_LIFE_CYCLE, DWConstant.MAX_LIFE_CYCLE));
        return lifecycle;
    }
}

