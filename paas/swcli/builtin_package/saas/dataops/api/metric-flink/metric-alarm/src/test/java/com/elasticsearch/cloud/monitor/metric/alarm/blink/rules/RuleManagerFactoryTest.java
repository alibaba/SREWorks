package com.elasticsearch.cloud.monitor.metric.alarm.blink.rules;

import com.elasticsearch.cloud.monitor.commons.rule.Rule;
import com.elasticsearch.cloud.monitor.metric.common.blink.mock.FakeFunctionContext;
import com.elasticsearch.cloud.monitor.metric.common.constant.Constants;
import com.elasticsearch.cloud.monitor.metric.common.rule.RuleManagerFactory;
import com.elasticsearch.cloud.monitor.metric.common.rule.EmonRulesManager;
import com.elasticsearch.cloud.monitor.metric.common.rule.util.RuleUtil;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;


/**
 * @author: fangzong.ly
 * @date: 2021/08/30 20:35
 */
public class RuleManagerFactoryTest {

    private FakeFunctionContext context;

    @Before
    public void setUp() throws Exception {
        RuntimeContext mockRuntimeContext = Mockito.mock(RuntimeContext.class);
        context = new FakeFunctionContext(mockRuntimeContext);
        context.setJobParameter(Constants.RULE_STORAGE_TYPE, "MINIO");

        context.setJobParameter(Constants.RULE_MINIO_ENDPOINT, "http://data-minio.ca221ae8860d9421688e59c8ab45c8b21.cn-hangzhou.alicontainer.com/");
        context.setJobParameter(Constants.RULE_MINIO_ACCESS_KEY, "admin");
        context.setJobParameter(Constants.RULE_MINIO_SECRET_KEY, "password");
        context.setJobParameter(Constants.RULE_MINIO_BUCKET, "metric-rules");
        context.setJobParameter(Constants.RULE_MINIO_FILE, "sreworks/rules.json");
    }

    @Test
    public void test() throws Exception {
        RuleManagerFactory ruleManagerFactory = RuleUtil.createRuleManagerFactoryForFlink(context);
        EmonRulesManager rulesManager = ruleManagerFactory.getRuleManager();
        List<Rule> rules = rulesManager.getAllRules();
        rules.forEach(rule -> System.out.println(rule));
        ruleManagerFactory.close();
    }
}
