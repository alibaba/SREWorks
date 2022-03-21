package com.alibaba.sreworks.domain.DTO;

import lombok.Data;

@Data
public class AppComponentRepoDetail {

    private String url;

    private String branch;

    private String dockerfileTemplate;

}
