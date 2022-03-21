package com.elasticsearch.cloud.monitor.metric.log.common;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.gson.Gson;

/**
 * @author xiaoping
 * @date 2021/3/8
 */
public class Util {
    public static Splitter searchSplitter = Splitter.on(" ").limit(14).omitEmptyStrings().trimResults();
    public static Splitter indexSplitter = Splitter.on(" ").limit(8).omitEmptyStrings().trimResults();
    public static Joiner joiner=Joiner.on(" ");
    public static Gson gson=new Gson();

    public static Double parseToMillis(String timeMayWithUnit) {
        char lastChar = timeMayWithUnit.charAt(timeMayWithUnit.length() - 1);
        if (lastChar >= '0' && lastChar <= '9') {
            return Double.valueOf(timeMayWithUnit);
        }

        if (timeMayWithUnit.endsWith("ms")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 2));
        }
        if (timeMayWithUnit.endsWith("min")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 3)) * 1000 * 60;
        }
        if (timeMayWithUnit.endsWith("h")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 1)) * 60 * 60 * 1000;
        }
        if (timeMayWithUnit.endsWith("us")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 2)) / 1000.0;
        }
        if (timeMayWithUnit.endsWith("ns")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 2)) / 1000000.0;
        }
        if (timeMayWithUnit.endsWith("s")) {
            return Double.parseDouble(timeMayWithUnit.substring(0, timeMayWithUnit.length() - 1)) * 1000;
        }

        throw new IllegalArgumentException(String.format("invalid input time %s", timeMayWithUnit));

    }

    public static Double getDigitFromString(String content, int start, int end) throws Exception {
        int i = start;
        for (; i <= end; i++) {
            if (content.charAt(i) < '0' || content.charAt(i) > '9') {
                break;
            }
        }
        if (start == i) {
            throw new Exception(String.format("getDigitFromString start %s of %s is not digital", start, content));
        }
        return Double.valueOf(content.substring(start, i));
    }
}
