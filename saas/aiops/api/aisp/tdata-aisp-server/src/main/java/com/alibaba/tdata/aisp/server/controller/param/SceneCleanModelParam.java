package com.alibaba.tdata.aisp.server.controller.param;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: SceneCleanModelParam
 * @Author: dyj
 * @DATE: 2022-03-03
 * @Description:
 **/
@Data
public class SceneCleanModelParam {
    @NotNull(message = "sceneCode can not be null!")
    @ApiModelProperty(notes = "场景Code", required = true)
    private String sceneCode;

    private List<String> keyList;
}
