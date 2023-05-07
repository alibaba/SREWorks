package com.alibaba.sreworks.job.master.params;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import com.alibaba.sreworks.job.utils.JsonUtil;
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
public class StreamJobPythonCreateParam {

    private String creator;

    private String operator;

    private String scriptName;

    private String scriptContent;

    private JSONArray options;

    public SreworksStreamJobBlock init(Long streamJobId, String appId) {
        return SreworksStreamJobBlock.builder()
            .gmtCreate(System.currentTimeMillis())
            .gmtModified(System.currentTimeMillis())
            .creator(getCreator())
            .blockType("python")
             .data(JsonUtil.map(
                "options", getOptions(),
                       "content", getScriptContent()
             ).toJSONString())
            .operator(getOperator())
            .appId(appId)
            .name(getScriptName())
            .streamJobId(streamJobId)
            .build();

    }

}
