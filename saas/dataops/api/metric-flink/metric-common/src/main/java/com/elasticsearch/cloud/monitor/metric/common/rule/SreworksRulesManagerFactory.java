package com.elasticsearch.cloud.monitor.metric.common.rule;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * SW规则管理生成类
 *
 * @author: fangzong.ly
 * @date: 2022/01/19 14:50
 */
@Slf4j
public abstract class SreworksRulesManagerFactory<T extends SreworksRulesManager> {
    protected Cache<String, T> rulesManagers;
    protected long RULE_REFRESH_PERIOD_DEF = 90000L;
    protected long RULE_REFRESH_SHUFFLE_DEF = 60000L;

    public SreworksRulesManagerFactory(Long refreshPeriod, Long shufflePeriod) {
        if (refreshPeriod != null) {
            RULE_REFRESH_PERIOD_DEF = refreshPeriod;
        }

        if (shufflePeriod != null) {
            RULE_REFRESH_SHUFFLE_DEF = shufflePeriod;
        }
        rulesManagers = CacheBuilder.newBuilder().expireAfterWrite(RULE_REFRESH_PERIOD_DEF, TimeUnit.MILLISECONDS).build();;
    }

    public SreworksRulesManagerFactory() {
        this(null, null);
    }

    /**
     * 规则管理实例化
     * @param rulesConfig
     */
    protected abstract T load(String rulesConfig);

    /**
     * 无租户, 根据规则ID返回规则管理对象
     *
     * @return
     */
    public T getRulesManager(String rulesKey) {
        T rulesManager = rulesManagers.getIfPresent(rulesKey);
        if (rulesManager == null) {
            log.warn(String.format("not exist rules manager[rules_manager_key:%s]", rulesKey));
        }
        return rulesManager;
    }

    /**
     * 无租户, 根据规则ID返回规则管理对象
     *
     * @return
     */
    public T getRulesManager(String rulesKey, String rulesConfig) {
        T rulesManager = rulesManagers.getIfPresent(rulesKey);
        if (rulesManager == null) {
            log.warn(String.format("not exist rules manager[rules_manager_key:%s]", rulesKey));
            rulesManager = load(rulesConfig);
            rulesManagers.put(rulesKey, rulesManager);
        }
        return rulesManager;
    }

    public void close() {
        rulesManagers.asMap().forEach((rulesKey, rulesManager) -> {
            rulesManager.stopUpdateTiming();
        });
        rulesManagers.cleanUp();
    }

    public Cache<String, T> getRulesManagers() {
        return rulesManagers;
    }

    public long getRULE_REFRESH_PERIOD_DEF() {
        return RULE_REFRESH_PERIOD_DEF;
    }

    public long getRULE_REFRESH_SHUFFLE_DEF() {
        return RULE_REFRESH_SHUFFLE_DEF;
    }
}
