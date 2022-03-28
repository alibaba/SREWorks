package com.elasticsearch.cloud.monitor.metric.common.rule.util;

import com.google.common.base.Joiner;

import java.util.TreeMap;


/**
 * @author wjp
 */
public class TreeMap2StringUtil {
    public static String parse(TreeMap treeMap){
        if(treeMap.isEmpty()){
            return "";
        }else{
            return Joiner.on(",").withKeyValueSeparator("=").join(treeMap);
        }
    }
}
