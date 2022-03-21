import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.utils.Requests;
import org.junit.Test;

import java.net.http.HttpResponse;
import java.util.List;


/**
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2022/02/09 10:51
 */
public class PlainTest {
    @Test
    public void test1() throws Exception {
        String pmdbEndpoint = "http://sreworks.c38cca9c474484bdc9873f44f733d8bcd.cn-beijing.alicontainer.com/gateway/pmdb";
        String metricMetaUrl = String.format("%s/metric/getMetricById?id=%s", pmdbEndpoint, 2);
        JSONObject headers = new JSONObject();
        headers.put("cookie", "_ga=GA1.2.1973809255.1642671321; tesla_user_id=admin; tesla_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0ZXNsYV91c2VyX2lkIjoiYWRtaW4iLCJleHAiOjE2NDQ4OTExNjQsIm5iZiI6MTY0NDI4NjI0NH0.wQaja9AUnqyMy7DIQzyBnJRjV0MFKg-WE4xFlot_SHU; lang=zh; country=CN; mp_9bfb1dea1874f9ea59453846a9ccd7d3_mixpanel=%7B%22distinct_id%22%3A%20%22MzEyMTg5Mzk3QHFxLmNvbQ%3D%3Dxm343yadf98%22%2C%22%24device_id%22%3A%20%2217e76d70f89646-0eaedab80d914-1d326253-384000-17e76d70f8b9c5%22%2C%22%24initial_referrer%22%3A%20%22%24direct%22%2C%22%24initial_referring_domain%22%3A%20%22%24direct%22%2C%22%24user_id%22%3A%20%22MzEyMTg5Mzk3QHFxLmNvbQ%3D%3Dxm343yadf98%22%7D; aliyun_lang=zh; aliyun_territory=CN; bcc_sso_token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbXBfaWQiOiI5OTk5OTk5OTkiLCJsb2dpbl9uYW1lIjoiYWRtaW4iLCJidWNfaWQiOiJudWxsIiwidXNlcl9pZCI6ImVtcGlkOjo5OTk5OTk5OTkiLCJuaWNrbmFtZSI6IueuoeeQhuWRmCIsImFsaXl1bl9wayI6Ijk5OTk5OTk5OSIsImV4cCI6MTY0NDQ1OTYxOCwibmJmIjoxNjQ0MzczMjE4fQ.3vbWvm9_o-IWLz6y5P_CcEyA-KIOhC4F7BGwf0NaqpQ");
        HttpResponse<String> metricResponse = Requests.get(metricMetaUrl, headers, null);
        if (metricResponse.statusCode() /100 == 2) {
            JSONObject body = JSONObject.parseObject(metricResponse.body());
            JSONObject metricMeta = body.getJSONObject("data");
            JSONObject metricLabels = metricMeta.getJSONObject("labels");
            System.out.println(metricMeta);
            System.out.println(metricLabels);

            String result = "[{\"id\": \"bbe351a01b1264625e686de32dca7f41\", \"metric_id\": 1, \"metric_name\": \"mall_backlog_order_hourly_cnt\", \"type\": \"\\u4e1a\\u52a1\\u6307\\u6807\", \"timestamp\": 1644376001454, \"value\": 4.85, \"labels\": {\"app_name\": \"xyz\", \"app_instance_id\": \"app_instance_id_1\", \"app_component_instance_id\": \"app_component_instance_id_2\"}}]";
            List<JSONObject> metricDatas = JSONObject.parseArray(result, JSONObject.class);
            for (JSONObject metricData: metricDatas) {
                metricLabels.putAll(metricData.getJSONObject("labels"));
                metricData.put("labels", metricLabels);
            }

            System.out.println(JSONObject.toJSONString(metricDatas));
        }
    }
}
