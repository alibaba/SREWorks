import com.alibaba.fastjson.JSONObject;
import lombok.Builder;
import lombok.Data;

/**
 * 故障实例
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2022/01/27 17:28
 */
@Data
@Builder
public class AlertInstance {

    Integer alertDefId;

    String appInstanceId;

    String appComponentInstanceId;

    String metricInstanceId;

    JSONObject metricInstanceLabels;

    Long occurTs;

    String source;

    String level;

    String receivers;

    String content;
}
