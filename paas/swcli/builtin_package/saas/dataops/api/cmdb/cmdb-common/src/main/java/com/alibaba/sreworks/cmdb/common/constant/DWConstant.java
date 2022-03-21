package com.alibaba.sreworks.cmdb.common.constant;

import com.alibaba.sreworks.cmdb.common.type.ColumnType;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class DWConstant {

    public static final String ENTITY_MODEL_NAME_REGEX = "^[A-Z][A-Z0-9_]{0,126}[A-Z0-9]$";

    public static final Pattern ENTITY_MODEL_NAME_PATTERN = Pattern.compile(ENTITY_MODEL_NAME_REGEX);

    public static final String ENTITY_MODEL_FIELD_REGEX = "^[a-z][a-z0-9_]{0,126}[a-z0-9]$";

    public static final Pattern ENTITY_MODEL_FIELD_PATTERN = Pattern.compile(ENTITY_MODEL_FIELD_REGEX);

    public static final String PARTITION_FIELD = "ds";
    public static final Boolean PARTITION_BUILD_IN = true;
    public static final ColumnType PARTITION_TYPE = ColumnType.STRING;
    public static final String PARTITION_DIM = "ds";
    public static final String PARTITION_ALIAS = "分区列";
    public static final Boolean PARTITION_NULLABLE = false;
    public static final String PARTITION_DESCRIPTION = "时间分区字段";


    public static final String PRIMARY_FIELD = "id";

    public static final List<String> MODEL_LAYERS = Arrays.asList("dim", "dwd", "dws", "ads");

    public static final String ENTITY_DW_LAYER = "ods";

    /**
     * 分区规范
     */
    public static final String PARTITION_BY_DAY = "{now/d}";
    public static final String PARTITION_BY_MONTH = "{now/M{yyyy.MM}}";

    /**
     * 生命周期
     */
    public static final Integer DEFAULT_LIFE_CYCLE = 365;
    public static final Integer MAX_LIFE_CYCLE = 3650;
    public static final Integer MIN_LIFE_CYCLE = 1;


}
