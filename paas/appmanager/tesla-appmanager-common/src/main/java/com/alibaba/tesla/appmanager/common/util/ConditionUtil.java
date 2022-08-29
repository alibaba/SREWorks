package com.alibaba.tesla.appmanager.common.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 组件实例 Condition Util
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class ConditionUtil {

    /**
     * 特定 Type: DataOutput
     */
    public static final String TYPE_DATA_OUTPUTS = "DataOutputs";

    /**
     * 构造单信息的 conditions JSONArray
     *
     * @param type    Type
     * @param status  Status
     * @param reason  Reason
     * @param message Message
     * @return JSONArray
     */
    public static JSONArray singleCondition(String type, String status, String reason, String message) {
        JSONObject condition = new JSONObject();
        condition.put("type", type);
        condition.put("status", status);
        condition.put("reason", reason);
        condition.put("message", message);
        JSONArray conditions = new JSONArray();
        conditions.add(condition);
        return conditions;
    }

    /**
     * 构造 DataOutput 类型的 Condition JSONArray
     *
     * @param dataOutput 输出 DataOutput 对象
     * @return JSONArray
     */
    public static JSONArray dataOutputCondition(JSONObject dataOutput) {
        JSONObject condition = new JSONObject();
        condition.put("type", TYPE_DATA_OUTPUTS);
        condition.put("status", "True");
        condition.put("reason", JSONObject.toJSONString(dataOutput));
        condition.put("message", "");
        JSONArray conditions = new JSONArray();
        conditions.add(condition);
        return conditions;
    }
}
