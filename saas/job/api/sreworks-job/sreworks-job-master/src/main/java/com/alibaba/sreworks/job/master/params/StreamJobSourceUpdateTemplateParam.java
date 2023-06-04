package com.alibaba.sreworks.job.master.params;

import com.alibaba.fastjson.JSONObject;
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
public class StreamJobSourceUpdateTemplateParam {

    private String operator;

    private String templateContent;

}
