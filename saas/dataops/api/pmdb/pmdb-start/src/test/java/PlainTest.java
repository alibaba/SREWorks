
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.pmdb.common.constant.MetricConstant;
import com.alibaba.sreworks.pmdb.domain.metric.MetricInstance;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import java.util.*;

public class PlainTest {

    @Test
    public void test1() {
        String metricName = "pod_cpu_usage";
        String indexPath = "kubernetes.pod.cpu.usage.node.pct";
        String indexTags = "service.type=kubernetes,metricset.name=pod,kubernetes.pod.name=dev-sreworks38-mall-7466869dcc-2mmsd";

        StringBuilder content = new StringBuilder();
        content.append(metricName).append(indexPath);

        if (StringUtils.isNotEmpty(indexTags)) {
            Map<String, String> tagsMap = new TreeMap<>();
            String[] kvTags = indexTags.split(MetricConstant.METRIC_TAGS_SPLITTER);
            for (String kvTag : kvTags) {
                String[] kvPair = kvTag.split("=");
                tagsMap.put(kvPair[0], kvPair[1]);
            }
            tagsMap.forEach((k ,v) -> content.append(k).append(v));
        }

        System.out.println(content);

        String digest = DigestUtils.md5DigestAsHex(content.toString().getBytes());
        System.out.println(digest);
    }

    @Test
    public void test2() {
        Integer metricId = 1;
        JSONObject labels = new JSONObject();
        labels.put("pig", "12");
        labels.put("allow", "88");
        labels.put("fly", "44");
        StringBuilder content = new StringBuilder();
        content.append(metricId).append(JSONObject.toJSONString(labels));
        System.out.println(JSONObject.toJSONString(labels));
        System.out.println(JSONObject.toJSONString(labels));
        System.out.println(DigestUtils.md5DigestAsHex(content.toString().getBytes()));

        JSONArray.parseArray(null);
    }

    @Test
    public void test3() {
        Map<String, MetricInstance>  newInstances = new HashMap<>();
        newInstances.put("k1", new MetricInstance());
        newInstances.put("k11", new MetricInstance());

        Map<String, MetricInstance>  oldInstances = new HashMap<>();
        oldInstances.put("k1", new MetricInstance());
        oldInstances.put("k2", new MetricInstance());



        HashSet<String> resSet = new HashSet<>();
        resSet.addAll(oldInstances.keySet());
        resSet.retainAll(newInstances.keySet());
        System.out.println(resSet);
        System.out.println(oldInstances);
    }

    @Test
    public void test4() {
        JSONObject labels = new JSONObject();
        labels.put("app_name", "test5");
        labels.put("app_id", "sreworks56");
        labels.put("$ref", "$[0].labels");
        System.out.println(labels.getJSONObject("abc"));
//        System.out.println(DigestUtils.md5DigestAsHex((2 + "|" + JSONObject.toJSONString(labels)).getBytes()));
    }

}
