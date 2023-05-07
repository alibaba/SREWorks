package com.alibaba.sreworks.job.master.domain.DTO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobRuntime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SreworksStreamJobRuntimeDTO {

    private Long id;

    private Long gmtCreate;

    private Long gmtModified;

    private String creator;

    private String operator;

    private String name;

    private String description;

    private JSONObject settings;

    private JSONArray tags;


    public SreworksStreamJobRuntimeDTO(SreworksStreamJobRuntime jobRuntime) {
        id = jobRuntime.getId();
        gmtCreate = jobRuntime.getGmtCreate();
        gmtModified = jobRuntime.getGmtModified();
        creator = jobRuntime.getCreator();
        operator = jobRuntime.getOperator();
        name = jobRuntime.getName();
        description = jobRuntime.getDescription();
        settings = JSONObject.parseObject(jobRuntime.getSettings());
        tags = JSONObject.parseArray(jobRuntime.getTags());
    }

}
