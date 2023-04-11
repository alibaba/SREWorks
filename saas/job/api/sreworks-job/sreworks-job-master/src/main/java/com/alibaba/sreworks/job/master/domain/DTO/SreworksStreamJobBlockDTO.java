package com.alibaba.sreworks.job.master.domain.DTO;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SreworksStreamJobBlockDTO {

    private Long id;

    private Long streamJobId;
    private Long gmtCreate;

    private Long gmtModified;

    private String creator;

    private String operator;

    private String appId;

    private String name;

    private String blockType;

    private String blockTypeDisplay;

    private JSONObject data;



    public SreworksStreamJobBlockDTO(SreworksStreamJobBlock jobBlock) {
        id = jobBlock.getId();
        gmtCreate = jobBlock.getGmtCreate();
        gmtModified = jobBlock.getGmtModified();
        streamJobId = jobBlock.getStreamJobId();
        appId = jobBlock.getAppId();
        name = jobBlock.getName();
        blockType = jobBlock.getBlockType();
        data = JSONObject.parseObject(jobBlock.getData());
        if (StringUtils.equals(blockType, "source") && data.getString("sourceType") != null) {
            blockTypeDisplay = "输入源:" + data.getString("sourceType");
        } else if (StringUtils.equals(blockType, "sink") && data.getString("sinkType") != null) {
            blockTypeDisplay = "输出:" + data.getString("sinkType");
        } else if (StringUtils.equals(blockType, "python")){
            blockTypeDisplay = "Python处理";
        } else {
            blockTypeDisplay = blockType;
        }
    }

}
