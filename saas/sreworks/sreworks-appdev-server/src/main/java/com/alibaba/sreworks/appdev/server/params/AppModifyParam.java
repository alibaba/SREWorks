package com.alibaba.sreworks.appdev.server.params;

import com.alibaba.sreworks.common.util.YamlUtil;
import com.alibaba.sreworks.domain.DO.App;

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
public class AppModifyParam {

    private String name;

    private String detailYaml;

    private String description;

    public void patchApp(App app, String operator) throws JsonProcessingException {
        app.setGmtModified(System.currentTimeMillis() / 1000);
        app.setLastModifier(operator);
        app.setName(name);
        app.setDescription(description);
        app.setDetail(YamlUtil.toJson(detailYaml));
    }

}
