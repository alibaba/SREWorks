package com.alibaba.sreworks.health.domain.bo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class SwModelField {
    Long id;

//    Date gmtCreate;
//
//    Date gmtModified;

    Long modelId;

    String field;

    String alias;

    String dim;

    String type;

//    Boolean buildIn;

    Boolean nullable;

//    String description;
}