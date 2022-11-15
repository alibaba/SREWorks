package com.alibaba.tesla.appmanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Definition Schema DTO
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefinitionSchemaDTO {

    /**
     * Schema 唯一标识
     */
    private String name;

    /**
     * Json Schema
     */
    private String jsonSchema;
}
