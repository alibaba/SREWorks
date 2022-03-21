package com.alibaba.sreworks.appdev.server.params;

import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DO.AppComponent;
import com.alibaba.sreworks.domain.DTO.AppComponentRepoDetail;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppComponentCreateParam {

    private String name;

    private String description;

    private String detailYaml;

    private String repoYaml;

    public AppComponentRepoDetail repo() throws JsonProcessingException {
        return YamlUtil.toObject(repoYaml, AppComponentRepoDetail.class);
    }

    public AppComponent toAppComponent(Long appId, String operator) throws JsonProcessingException {
        return AppComponent.builder()
            .gmtCreate(System.currentTimeMillis() / 1000)
            .gmtModified(System.currentTimeMillis() / 1000)
            .creator(operator)
            .lastModifier(operator)
            .appId(appId)
            .name(getName())
            .repoDetail(YamlUtil.toJson(repoYaml))
            .detail(YamlUtil.toJson(detailYaml))
            .description(getDescription())
            .build();
    }

}
