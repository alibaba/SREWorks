package com.elasticsearch.cloud.monitor.metric.log.udtf.sync;

import com.elasticsearch.cloud.monitor.metric.common.sync.OssSync;
import com.elasticsearch.cloud.monitor.metric.common.sync.OssSync.OssSyncCallBack;
import com.elasticsearch.cloud.monitor.metric.log.common.Constant;
import com.elasticsearch.cloud.monitor.metric.log.common.Util;
import com.elasticsearch.cloud.monitor.metric.log.udtf.sync.EsInfo.EcsInfo;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.opensearch.cobble.monitor.Monitor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.flink.table.functions.FunctionContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author xiaoping
 * @date 2021/3/30
 */
@Slf4j
public class EsInfoSync extends OssSyncCallBack<List<EsInfo>> {
    public final static String ES_INFO_OSS_ENDPOINT = "es.info.oss.endPoint";
    public final static String ES_INFO_OSS_ACCESS_KEY = "es.info.oss.access.key";
    public final static String ES_INFO_OSS_ACCESS_SECRET = "es.info.oss.access.secret";
    public final static String ES_INFO_OSS_BUCKET = "es.info.oss.bucket";
    public final static String ES_INFO_OSS_FILE = "es.info.oss.file";
    private OssSync<List<EsInfo>> ossSync;
    private volatile Map<String, EsInfo> instanceToInfo = Maps.newConcurrentMap();

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private volatile boolean inited = false;
    private Set<String> unknownInstances = Sets.newConcurrentHashSet();
    private Monitor monitor;
    private Long lastCheckUnknownTimestamp = System.currentTimeMillis();

    public EsInfoSync(FunctionContext context, Monitor monitor) {
        ossSync = new OssSync<>(context.getJobParameter(ES_INFO_OSS_ENDPOINT, ""),
            context.getJobParameter(ES_INFO_OSS_ACCESS_KEY, ""),
            context.getJobParameter(ES_INFO_OSS_ACCESS_SECRET, ""),
            context.getJobParameter(ES_INFO_OSS_BUCKET, ""),
            context.getJobParameter(ES_INFO_OSS_FILE, ""));
        ossSync.registerCallBack(this, new TypeToken<List<EsInfo>>() {}.getType());
        this.monitor = monitor;
    }

    @Override
    public void update(List<EsInfo> esInfos) throws InterruptedException {
        if (esInfos != null) {
            Map<String, EsInfo> temp = Maps.newConcurrentMap();
            for (EsInfo esInfo : esInfos) {
                temp.put(esInfo.getInstanceId(), esInfo);
            }
            lock.writeLock().lock();
            instanceToInfo = temp;
            lock.writeLock().unlock();
        }
    }

    @Override
    public void ossSyncOccur() throws InterruptedException {
        inited = true;
    }

    public boolean addBizTag(String instanceId, String ip, Map<String, Object> data) throws InterruptedException {
        if (data == null) {
            return false;
        }
        while (!inited) {
            log.error("wait es info oss sync inited");
            Thread.sleep(3000);
        }
        lock.readLock().lock();
        Map<String, EsInfo> temp = instanceToInfo;
        lock.readLock().unlock();
        if (temp.containsKey(instanceId)) {
            EsInfo esInfo = temp.get(instanceId);
            if (StringUtils.isNotEmpty(esInfo.getUid())) {
                data.put(Constant.ES_RESOURCE_UID, esInfo.getUid());
            }
            if (StringUtils.isNotEmpty(esInfo.getResourceGroup())) {
                data.put(Constant.ES_RESOURCE_GROUP, esInfo.getResourceGroup());
            }
            if (StringUtils.isNotEmpty(ip)) {
                if (esInfo.getIpToZone() != null && esInfo.getIpToZone().containsKey(ip)) {
                    data.put(Constant.ES_AVAILABLE_ZONE, esInfo.getIpToZone().get(ip));
                }
            }
            if (StringUtils.isNotEmpty(esInfo.getRegionId())) {
                data.put(Constant.regionId, esInfo.getRegionId());
            }
            return true;
        } else {
            unknownInstances.add(instanceId);
            if (System.currentTimeMillis() - lastCheckUnknownTimestamp > TimeUnit.MINUTES.toMillis(2)) {
                log.error(String.format("unknown instance %s", Util.gson.toJson(unknownInstances)));
                unknownInstances.clear();
                lastCheckUnknownTimestamp = System.currentTimeMillis();
            }
            if (monitor != null) {
                monitor.increment(Constant.LOG_UNKNOWN_INSTANCE_COUNT, 1);
            }
        }
        return false;
    }

    public boolean addBizTagByEcsId(String instanceId, String ecsId, Map<String, Object> data)
        throws InterruptedException {
        if (data == null || StringUtils.isEmpty(instanceId) || !instanceId.startsWith("es") || StringUtils.isEmpty(
            ecsId)) {
            return false;
        }

        while (!inited) {
            log.error("wait es info oss sync inited");
            Thread.sleep(3000);
        }
        lock.readLock().lock();
        Map<String, EsInfo> temp = instanceToInfo;
        lock.readLock().unlock();
        if (temp.containsKey(instanceId)) {
            EsInfo esInfo = temp.get(instanceId);
            String foundIp = null;
            if (esInfo.getNodeInfoMap() != null) {
                for (EcsInfo ecsInfo : esInfo.getNodeInfoMap().values()) {
                    if (ecsId.equals(ecsInfo.getEcsId())) {
                        foundIp = ecsInfo.getEcsIp();
                        break;
                    }
                }
            }
            if (StringUtils.isNotEmpty(foundIp)) {
                data.put(Constant.HOST, foundIp);
                return addBizTag(instanceId, foundIp, data);
            } else {
                log.error(String.format("can not get ecs ip of es %s ecsId %s", instanceId, ecsId));
            }
        } else {
            unknownInstances.add(instanceId);
            if (System.currentTimeMillis() - lastCheckUnknownTimestamp > TimeUnit.MINUTES.toMillis(2)) {
                log.error(String.format("unknown instance %s", Util.gson.toJson(unknownInstances)));
                unknownInstances.clear();
                lastCheckUnknownTimestamp = System.currentTimeMillis();
            }
            if (monitor != null) {
                monitor.increment(Constant.LOG_UNKNOWN_INSTANCE_COUNT, 1);
            }
        }
        return false;
    }

    public void close() {
        if (ossSync != null) {
            ossSync.close();
        }
    }
}
