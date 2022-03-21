package com.alibaba.sreworks.appdev.server.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoDetail {

    private String url;

    private String branch;

    private String user;

    private String password;

    private String dockerfileTemplate;

}
