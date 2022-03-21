package com.alibaba.tdata.aisp.server.controller.param;

import javax.validation.constraints.NotNull;

import com.alibaba.fastjson.JSONObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: SceneUpsertModelParam
 * @Author: dyj
 * @DATE: 2022-03-03
 * @Description:
 **/
@Data
public class SceneUpsertModelParam {
    @NotNull(message = "sceneCode can not be null!")
    @ApiModelProperty(notes = "场景Code", required = true)
    private String sceneCode;

    @NotNull(message = "sceneModelParam can not be null!")
    @ApiModelProperty(notes = "场景级别modelParam", required = true)
    private JSONObject sceneModelParam;
}
