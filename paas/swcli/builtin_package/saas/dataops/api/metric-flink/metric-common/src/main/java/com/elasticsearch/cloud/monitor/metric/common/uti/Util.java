package com.elasticsearch.cloud.monitor.metric.common.uti;

import com.google.common.base.Joiner;
import com.google.common.base.Joiner.MapJoiner;
import com.google.common.base.Splitter;
import com.google.common.base.Splitter.MapSplitter;
import com.google.gson.Gson;

/**
 * @author xiaoping
 * @date 2019/12/29
 */
public class Util {
    public static MapJoiner joiner= Joiner.on(",").withKeyValueSeparator("=");
    public static MapSplitter splitter= Splitter.on(",").withKeyValueSeparator("=");

    public static Joiner valueJoiner= Joiner.on(",");
    public static Splitter valueSplitter= Splitter.on(",");
    public static Splitter valuesSplitter= Splitter.on(" ").omitEmptyStrings().trimResults();

    public static Gson gson=new Gson();
}
