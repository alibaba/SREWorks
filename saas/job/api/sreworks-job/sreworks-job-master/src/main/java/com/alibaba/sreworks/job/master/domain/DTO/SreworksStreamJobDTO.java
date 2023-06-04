package com.alibaba.sreworks.job.master.domain.DTO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksJob;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import com.alibaba.sreworks.job.master.event.JobEventConf;
import com.alibaba.sreworks.job.utils.StringUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SreworksStreamJobDTO {

    private Long id;

    private Long gmtCreate;

    private Long gmtModified;

    private String creator;

    private String operator;

    private String appId;

    private String name;

    private String alias;

    private JSONArray tags;

    private String description;

    private JSONObject options;

    private String jobType;

    private String status;

    public SreworksStreamJobDTO(SreworksStreamJob job) {
        if (job != null) {
            id = job.getId();
            gmtCreate = job.getGmtCreate();
            gmtModified = job.getGmtModified();
            appId = job.getAppId();
            name = job.getName();
            status = job.getStatus();
            alias = job.getAlias();
            tags = JSONObject.parseArray(job.getTags());
            creator = job.getCreator();
            operator = job.getOperator();
            description = job.getDescription();
            options = JSONObject.parseObject(job.getOptions());
            jobType = job.getJobType();
        }
    }

}
