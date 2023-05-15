package com.alibaba.tesla.appmanager.server.service.componentpackage.instance;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import com.alibaba.tesla.appmanager.autoconfig.SystemProperties;
import com.alibaba.tesla.appmanager.common.util.ExceptionUtil;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.constant.PodStatusPhaseEnum;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.dto.WaitKanikoBuildPod;
import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.util.BuildUtil;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.util.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

/**
 * @ClassName: KanikoBuildPodStateChecker
 * @Author: dyj
 * @DATE: 2023-05-10
 * @Description:
 **/
@Slf4j
@Component
public class KanikoBuildPodStateChecker {
    private static List<WaitKanikoBuildPod> waitKanikoBuildPodList = new CopyOnWriteArrayList<>();
    private CoreV1Api api;
    @Autowired
    private SystemProperties systemProperties;

    @PostConstruct
    public void init() throws IOException {
        ApiClient client = ClientBuilder.defaultClient();
        OkHttpClient httpClient = client.getHttpClient().newBuilder().readTimeout(3, TimeUnit.SECONDS).build();
        client.setHttpClient(httpClient);
        api = new CoreV1Api(client);
    }

    public void add(WaitKanikoBuildPod waitKanikoBuildPod) {
        waitKanikoBuildPodList.add(waitKanikoBuildPod);
    }

    public void clean(WaitKanikoBuildPod waitKanikoBuildPod) {
        waitKanikoBuildPodList.remove(waitKanikoBuildPod);
    }

    @Scheduled(fixedDelay = 20*1000)
    public void checkStatus() {
        if (CollectionUtils.isEmpty(waitKanikoBuildPodList)) {
            return;
        }
        Iterator<WaitKanikoBuildPod> iterator = waitKanikoBuildPodList.iterator();
        while (iterator.hasNext()) {
            WaitKanikoBuildPod waitKanikoBuildPod = iterator.next();
            if (waitKanikoBuildPod!=null) {
                try {
                    if (waitKanikoBuildPod.haveFailed() || waitKanikoBuildPod.haveSucceed()) {
                        continue;
                    } else {
                        if (CollectionUtils.isEmpty(waitKanikoBuildPod.getRunningPods())) {
                            continue;
                        }
                        HashSet<String> runningPods = new HashSet<>(waitKanikoBuildPod.getRunningPods());
                        for (String runningPod : runningPods) {
                            try {
                                V1Pod v1Pod = api.readNamespacedPodStatus(runningPod, systemProperties.getK8sNamespace(), null);
                                String podLog = null;
                                try {
                                    podLog = api.readNamespacedPodLog(runningPod, systemProperties.getK8sNamespace(),
                                        null, null, null, null, null, null, null, null, null);
                                } catch (ApiException e) {
                                    log.warn("action=checkTaskStatus|| Can not read pod log, pod:{}", runningPod, e);
                                    podLog = String.format("Read pod log failed! Exception:%s", ExceptionUtil.getStackTrace(e));
                                }
                                if (PodStatusPhaseEnum.Failed.name().equalsIgnoreCase(v1Pod.getStatus().getPhase())) {
                                    waitKanikoBuildPod.changeStatus(runningPod, PodStatusPhaseEnum.Failed, BuildUtil.genLogContent(runningPod, podLog));
                                } else if (PodStatusPhaseEnum.Succeeded.name().equalsIgnoreCase(v1Pod.getStatus().getPhase())) {
                                    waitKanikoBuildPod.changeStatus(runningPod, PodStatusPhaseEnum.Succeeded, BuildUtil.genLogContent(runningPod, podLog));
                                }
                            } catch (ApiException e) {
                                log.warn("action=checkTaskStatus|| can not read pod:{} status!", runningPod, e);
                                waitKanikoBuildPod.addAccessError();
                                if (waitKanikoBuildPod.getAccessError()>=10) {
                                    String message = String.format("\nCan not read pod:%s status! \nException:%s", runningPod, ExceptionUtil.getStackTrace(e));
                                    waitKanikoBuildPod.appendLog(BuildUtil.genLogContent(runningPod, message));
                                    waitKanikoBuildPod.changeStatus(runningPod, PodStatusPhaseEnum.Failed, BuildUtil.genLogContent(runningPod, message));
                                }
                                break;
                            }
                        }

                    }
                    if (DateUtils.addHours(waitKanikoBuildPod.getStart(), 3).before(new Date())) {
                        String logContent = "action=checkTaskStatus|| Build task have been timeout: 3 hours!";
                        waitKanikoBuildPod.appendLog(logContent);
                        waitKanikoBuildPod.setIsFailed(true);
                    }
                } catch (Exception e) {
                    log.error("action=checkTaskStatus|| Check task status have error!", e);
                    String logContent = String.format("action=checkTaskStatus|| Check task status have error! Exception:%s", ExceptionUtil.getStackTrace(e));
                    waitKanikoBuildPod.appendLog(logContent);
                    waitKanikoBuildPod.setIsFailed(true);
                }
            }
        }

    }
}
