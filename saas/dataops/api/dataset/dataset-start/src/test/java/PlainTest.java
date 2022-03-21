import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.dataset.domain.primary.InterfaceConfig;
import com.alibaba.sreworks.dataset.processors.ESMetricAggType;
import com.google.common.collect.Maps;
import com.hubspot.jinjava.Jinjava;
import io.swagger.models.auth.In;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.junit.Test;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlainTest {

    @Test
    public void test1() throws Exception {
        String[] s = {"a","b","c"};
        List list = java.util.Arrays.asList(s);
        List<String> newList = new ArrayList<>(list);
        String o = newList.remove(0);
        System.out.println(o);

        List<String> dd = Arrays.asList("a.b.c".split("\\."));
        System.out.println(dd.size());
    }

    @Test
    public void test2() throws Exception {
        Object src = "20180918a";
        int dd = Integer.parseInt(String.valueOf(src));
        System.out.println(dd);
    }

    @Test
    public void test3() throws Exception {
        String dim = "_source.abc.tedt.ddd";
        if (dim.startsWith("_source")) {
            dim = dim.substring(dim.indexOf(".") + 1);
        }

        List<String> dims = Arrays.asList(dim.split("\\."));

        System.out.println(dims);
    }


    @Test
    public void test4() throws Exception {
        InterfaceConfig interfaceConfig = JSONObject.toJavaObject(null, InterfaceConfig.class);
        System.out.println(interfaceConfig);
    }

    @Test
    public void test5() throws Exception {
        String content = "#################################################\n" +
                "## mysql serverId , v1.0.26+ will autoGen\n" +
                "# canal.instance.mysql.slaveId=0\n" +
                "\n" +
                "# enable gtid use true/false\n" +
                "canal.instance.gtidon=false\n" +
                "\n" +
                "# position info\n" +
                "canal.instance.master.address = appmanager-mysql.default.svc.cluster.local:3306\n" +
                "canal.instance.master.journal.name=\n" +
                "canal.instance.master.position=\n" +
                "canal.instance.master.timestamp=\n" +
                "canal.instance.master.gtid=\n" +
                "\n" +
                "\n" +
                "# username/password\n" +
                "canal.instance.dbUsername = root\n" +
                "canal.instance.dbPassword = bguwsawqm6k\n" +
                "canal.instance.connectionCharset = UTF-8\n" +
                "\n" +
                "# enable druid Decrypt database password\n" +
                "canal.instance.enableDruid=false\n" +
                "# canal.instance.pwdPublicKey=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALK4BUxdDltRRE5/zXpVEVPUgunvscYFtEip3pmLlhrWpacX7y7GCMo2/JM6LeHmiiNdH1FWgGCpUfircSwlWKUCAwEAAQ==\n" +
                "\n" +
                "# table regex\n" +
                "canal.instance.filter.regex=sreworks\\..*\n" +
                "# table black regex\n" +
                "canal.instance.filter.black.regex=sreworksappmanager\\..*\n" +
                "# table field filter(format: schema1.tableName1:field1/field2,schema2.tableName2:field1/field2)\n" +
                "#canal.instance.filter.field=test1.t_product:id/subject/keywords,test2.t_company:id/name/contact/ch\n" +
                "# table field black filter(format: schema1.tableName1:field1/field2,schema2.tableName2:field1/field2)\n" +
                "#canal.instance.filter.black.field=test1.t_product:subject/product_image,test2.t_company:id/name/contact/ch\n" +
                "\n" +
                "# mq config\n" +
                "canal.mq.topic=sreworks_data_topic\n" +
                "#canal.mq.dynamicTopic=mytest,.*,mytest.user,mytest\\..*,.*\\..*\n" +
                "canal.mq.partition=0\n" +
                "# hash partition config\n" +
                "#canal.mq.partitionsNum=3\n" +
                "#canal.mq.partitionHash=mytest.person:id,mytest.role:id\n" +
                "#################################################";
        System.out.println(SecurityUtil.md5String(content));
    }

    @Test
    public void test6() throws Exception {
        String result = String.join(",", "a", null, "b", "c");
        System.out.println(result);
        String test  = StringEscapeUtils.escapeJava("\"a\"def");
        System.out.println(test);
    }

    @Test
    public void regextExtractNumber(){

        String sql = "SELECT id as id,name as name  FROM {{team}}  WHERE id={{id}}";
        // 匹配任意的数字
        Pattern p = Pattern.compile("\\{\\{(.*?)}}");
        Matcher m = p.matcher(sql);
        while(m.find()) {
            System.out.println(m.group());
        }
        String sqlTemplateWithPlaceholder = sql.replaceAll("(\\{\\{)", "\\${");
        System.out.println(sqlTemplateWithPlaceholder);

        List a = Arrays.asList(new String[]{"1","2"});

        List<String> list2 = new ArrayList<>();
        list2.add("性别");
        list2.add("出生日期");


        a.addAll(list2);
        System.out.println(a);
    }

    @Test
    public void test7() throws Exception {
        String queryFieldsStr = "[{\"type\":\"STRING\",\"field\":\"appId\",\"description\":\"应用ID\"}]";
        JSONArray.parseArray(queryFieldsStr);
    }

    @Test
    public void test8() throws Exception {
        Jinjava jinjava = new Jinjava();
        Map<String, Object> context = Maps.newHashMap();
        context.put("name", "Jared");
        context.put("isFull", true);


        String renderedTemplate = jinjava.render("Hello, {{ name }} {{isFull}}!", context);
        System.out.println(renderedTemplate);
    }

}
