package com.alibaba.tdata.aisp.server.controller.param;

import java.util.List;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @ClassName: SceneQueryParam
 * @Author: dyj
 * @DATE: 2021-11-18
 * @Description:
 **/
@Data
@ApiModel(value = "根据条件查找场景")
public class SceneQueryParam {
    private String sceneCode;

    private String sceneCodeLike;

    private String owners;

    private String sceneName;

    private String sceneNameLike;

    private List<String> productList;

    @NotNull(message = "page can not be null!")
    @ApiModelProperty(required = true)
    private Integer page;

    @NotNull(message = "pageSize can not be null!")
    @ApiModelProperty(required = true)
    private Integer pageSize;
}
