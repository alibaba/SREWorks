package com.alibaba.sreworks.job.master.params;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamJobCreateParam {

    private String creator;

    private String operator;

    private String appId;

    private String name;

    private String alias;

    private JSONArray tags;

    private String description;

    private JSONObject options;

    private String jobType;

    private Long executionPoolId;

    private MultipartFile file;

    public SreworksStreamJob job() {
        return SreworksStreamJob.builder()
            .gmtCreate(System.currentTimeMillis())
            .gmtModified(System.currentTimeMillis())
            .creator(getCreator())
            .operator(getOperator())
            .appId(getAppId())
            .name(getName())
            .executionPoolId(getExecutionPoolId())
            .alias(getAlias())
            .tags(JSONObject.toJSONString(getTags()))
            .description(getDescription())
            .options(JSONObject.toJSONString(getOptions()))
            .jobType(getJobType())
            .build();

    }

}
