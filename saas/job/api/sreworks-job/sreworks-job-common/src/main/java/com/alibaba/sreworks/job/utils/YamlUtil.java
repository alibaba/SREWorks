package com.alibaba.sreworks.job.utils;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

/**
 * @author jiongen.zje
 */
public class YamlUtil {

    public static String jsonObjectToYaml(JSONObject jsonObject) throws JsonProcessingException {

        ObjectMapper objectMapper = new ObjectMapper();
        Object javaObj = objectMapper.readValue(jsonObject.toJSONString(), Object.class);

        // 使用Jackson库将Java对象转换为YAML格式
        YAMLMapper yamlMapper = new YAMLMapper();
        String yamlStr = yamlMapper.writeValueAsString(javaObj);
        return yamlStr.replaceFirst("^---\n", "");

    }
}
