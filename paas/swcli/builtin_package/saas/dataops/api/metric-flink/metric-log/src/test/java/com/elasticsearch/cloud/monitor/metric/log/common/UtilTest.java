package com.elasticsearch.cloud.monitor.metric.log.common;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author xiaoping
 * @date 2021/3/11
 */
public class UtilTest {

    @Test
    public void testConvert() {
        Assert.assertEquals(12.0, Util.parseToMillis("12"), 0.00001);
        Assert.assertEquals(12000.0, Util.parseToMillis("12s"), 0.00001);
        Assert.assertEquals(12.0, Util.parseToMillis("12ms"), 0.00001);
        Assert.assertEquals(12000.0 * 60, Util.parseToMillis("12min"), 0.00001);
        Assert.assertEquals(12.0 * 60 * 60 * 1000, Util.parseToMillis("12h"), 0.00001);
        Assert.assertEquals(12.0 /1000000, Util.parseToMillis("12ns"), 0.0000001);
        Assert.assertEquals(12.0 /1000, Util.parseToMillis("12us"), 0.0000001);
    }

}
