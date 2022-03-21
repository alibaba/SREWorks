package com.alibaba.tesla;

import static org.junit.Assert.assertTrue;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.tkgone.server.common.StringFun;
import com.alibaba.tesla.tkgone.server.common.Tools;
import netscape.javascript.JSObject;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        String ss = "etlJsonGet({\"redirect\":{},\"isTree\":false,\"pageLayoutType\":\"CUSTOM\",\"keywords\":\"\",\"hidden\":true,\"icon\":\"\",\"label\":\"single_cluster\",\"type\":\"custom\",\"roleList\":[\"cluster:guest\"],\"menuBarHeader\":\"$(clusterName)<a style={{fontSize:14,marginLeft:12}} href='/#/cluster'><Icon type='rollback'>返回</a>\",\"url\":\"cluster/single_cluster\",\"layout\":\"custom\",\"selectPathInHidden\":\"\",\"isDefault\":false,\"name\":\"single_cluster\",\"order\":1}, url)";
        System.out.println(StringFun.runWithOutException(ss));
    }

    @Test
    public void test1()
    {
        String configStr = "{\n" +
                "          \"config\" : {\n" +
                "            \"redirect\" : { },\n" +
                "            \"isTree\" : false,\n" +
                "            \"pageLayoutType\" : \"CUSTOM\",\n" +
                "            \"keywords\" : \"\",\n" +
                "            \"hidden\" : true,\n" +
                "            \"icon\" : \"\",\n" +
                "            \"label\" : \"集群详情\",\n" +
                "            \"type\" : \"custom\",\n" +
                "            \"roleList\" : [\n" +
                "              \"cluster:guest\"\n" +
                "            ],\n" +
                "            \"menuBarHeader\" : \"$(clusterName)<a style={{fontSize:14,marginLeft:12}} href='/#/cluster'><Icon type='rollback'>返回</a>\",\n" +
                "            \"url\" : \"cluster/single_cluster\",\n" +
                "            \"layout\" : \"custom\",\n" +
                "            \"selectPathInHidden\" : \"\",\n" +
                "            \"isDefault\" : false,\n" +
                "            \"name\" : \"single_cluster\",\n" +
                "            \"order\" : 1\n" +
                "          },\n" +
                "          \"__label\" : \"站点 导航 站点导航 前端\",\n" +
                "          \"parent_node_type_path\" : \"cluster|app|T:\"\n" +
                "        }";
        String ss = "etlJsonGet(${config}, label) etlJsonGet(${config}, keywords)";

        System.out.println(Tools.processTemplateString(ss, JSONObject.parseObject(configStr)));
    }
}
