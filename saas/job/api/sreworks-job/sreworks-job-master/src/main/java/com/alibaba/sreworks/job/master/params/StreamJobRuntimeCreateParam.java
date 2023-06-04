package com.alibaba.sreworks.job.master.params;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobRuntime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamJobRuntimeCreateParam {

    private String creator;

    private String operator;

    private String name;

    private String description;

    private JSONObject settings;

    public SreworksStreamJobRuntime init() {
        return SreworksStreamJobRuntime.builder()
            .gmtCreate(System.currentTimeMillis())
            .gmtModified(System.currentTimeMillis())
            .creator(getCreator())
            .operator(getOperator())
            .name(getName())
            .description(getDescription())
            .settings(JSONObject.toJSONString(getSettings()))
            .build();

    }

}
