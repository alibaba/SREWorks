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
public class StreamJobSourceCreateParam {

    private String creator;

    private String operator;

    private String sourceName;

    private JSONArray options;

    private JSONArray columns;

    private String type;

    public SreworksStreamJobBlock init(Long streamJobId, String appId) {
        return SreworksStreamJobBlock.builder()
            .gmtCreate(System.currentTimeMillis())
            .gmtModified(System.currentTimeMillis())
            .creator(getCreator())
            .blockType("source")
             .data(JsonUtil.map(
             "options", getOptions(),
                    "columns", getColumns(),
                     "sourceType", getType()
             ).toJSONString())
            .operator(getOperator())
            .appId(appId)
            .name(getSourceName())
            .streamJobId(streamJobId)
            .build();

    }

}
