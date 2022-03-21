package com.elasticsearch.cloud.monitor.metric.common.uti;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.elasticsearch.cloud.monitor.metric.common.rule.constant.StorageType;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author xiaoping
 * @date 2021/5/22
 */
public class UtilTest {

    @Test
    public void test(){
        System.out.println(System.currentTimeMillis()/ TimeUnit.HOURS.toMillis(1));
    }

    @Test
    public void test1(){
        StorageType storageType = StorageType.valueOf("MINIO");
        System.out.println(storageType);
    }

    @Test
    public void test2(){
        Object metricValue = 100L;
        Number numberValue = (Number) metricValue;
        Double value = numberValue.doubleValue();
        System.out.println(value);
    }

    @Test
    public void test3() {
        System.out.println(PropertiesUtil.getProperty("default.minio.secret_key"));
    }

}
