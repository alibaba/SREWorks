package com.elasticsearch.cloud.monitor.metric.common.rule;

import com.elasticsearch.cloud.monitor.metric.common.client.MinioConfig;
import lombok.extern.slf4j.Slf4j;


/**
 * minio规则管理生成类
 *
 * @author: fangzong.ly
 * @date: 2021/08/31 21:15
 */
@Slf4j
public class RuleMinioManagerFactory extends RuleManagerFactory {
    private MinioConfig minioConfig;

    public RuleMinioManagerFactory(MinioConfig minioConfig, Long refreshPeriod, Long shufflePeriod) {
        super(minioConfig, refreshPeriod, shufflePeriod);
    }

    @Override
    protected void init(Object clientConfig) {
        minioConfig = (MinioConfig)clientConfig;
        EmonRulesManager rulesManager = new RulesMinioManager(minioConfig);
        rulesManager.startShuffleTimingUpdate(this.RULE_REFRESH_PERIOD_DEF, this.RULE_REFRESH_SHUFFLE_DEF);
        this.rulesManagers.put(minioConfig.getBucket() + '/' + minioConfig.getFile(), rulesManager);
    }
}
