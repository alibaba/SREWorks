package com.alibaba.sreworks.job.master.domain.DTO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SreworksStreamJobSinkDTO {

    private Long id;

    private Long streamJobId;
    private Long gmtCreate;

    private Long gmtModified;

    private String creator;

    private String operator;

    private String appId;

    private String name;

    private JSONArray options;

    private JSONArray columns;


    public SreworksStreamJobSinkDTO(SreworksStreamJobBlock jobBlock) {
        id = jobBlock.getId();
        gmtCreate = jobBlock.getGmtCreate();
        gmtModified = jobBlock.getGmtModified();
        streamJobId = jobBlock.getStreamJobId();
        appId = jobBlock.getAppId();
        name = jobBlock.getName();
        JSONObject data = JSONObject.parseObject(jobBlock.getData());
        options = data.getJSONArray("options");
        columns = data.getJSONArray("columns");
    }

}
