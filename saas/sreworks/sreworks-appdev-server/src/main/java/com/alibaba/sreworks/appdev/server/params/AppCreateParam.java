package com.alibaba.sreworks.appdev.server.params;

import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DO.App;
import com.alibaba.sreworks.domain.DTO.AppDetail;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author jinghua.yjh
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppCreateParam {

    private Long teamId;

    private String name;

    private String description;

    private AppDetail detail;

    private String detailYaml;

    public App toApp(String operator) throws JsonProcessingException {
        return App.builder()
            .gmtCreate(System.currentTimeMillis() / 1000)
            .gmtModified(System.currentTimeMillis() / 1000)
            .creator(operator)
            .lastModifier(operator)
            .teamId(teamId)
            .name(name)
            .detail(YamlUtil.toJson(detailYaml))
            .description(description)
            .build();
    }

}
