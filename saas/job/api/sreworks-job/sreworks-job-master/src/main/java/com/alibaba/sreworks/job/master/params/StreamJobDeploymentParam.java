package com.alibaba.sreworks.job.master.params;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import com.alibaba.sreworks.job.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamJobDeploymentParam {

    private String flinkImage;

    private String flinkVersion;

    private String jarUri;

    private String entryClass;

    private String name;

    private JSONObject resources;

    private JSONArray extAdditionalDependencies;

    private JSONArray additionalDependencies;

    private String pyArchives;

    private String pyClientExecutable;

    private String pythonScriptName;

    private Map<String, String> execParams;

}
