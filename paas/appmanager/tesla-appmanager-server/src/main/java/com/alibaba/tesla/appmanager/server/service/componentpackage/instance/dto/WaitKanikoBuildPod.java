package com.alibaba.tesla.appmanager.server.service.componentpackage.instance.dto;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.alibaba.tesla.appmanager.server.service.componentpackage.instance.constant.PodStatusPhaseEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

/**
 * @ClassName: WaitKanikoBuildPod
 * @Author: dyj
 * @DATE: 2022-08-31
 * @Description:
 **/
@Slf4j
@Data
public class WaitKanikoBuildPod {

    private Set<String> runningPods;
    private Set<String> successPods;
    private Set<String> failedPods;
    private StringBuffer logBuffer;
    private Integer accessError;
    private Boolean isFailed;
    private Date start;

    public WaitKanikoBuildPod() {
        runningPods = new HashSet<>();
        successPods = new HashSet<>();
        failedPods = new HashSet<>();
        logBuffer = new StringBuffer();
        accessError = 0;
        isFailed = false;
        start = new Date();
    }

    public void changeStatus(String pod, PodStatusPhaseEnum status, String logContent) {
        log.info("action=changeKanikoPodStatus || pod:{} change to status:{}", pod, status);
        switch (status) {
            case Succeeded: {
                runningPods.remove(pod);
                successPods.add(pod);
                logBuffer.append(logContent);
                break;
            }
            case Failed: {
                isFailed = true;
                runningPods.remove(pod);
                failedPods.add(pod);
                logBuffer.append(logContent);
                break;
            }
            default: break;
        }
    }

    public Set<String> addRunning(String pod) {
        runningPods.add(pod);
        return runningPods;
    }

    public void addAccessError() {
        accessError+=1;
    }

    public String getLog() {
        return logBuffer.toString();
    }

    public void appendLog(String message) {
        logBuffer.append(message);
    }

    public boolean haveSucceed() {
        if (CollectionUtils.isEmpty(runningPods) && CollectionUtils.isEmpty(failedPods)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean haveFailed() {
        return isFailed;
    }

    public Set<String> getAllPods() {
        Set<String> res = new HashSet<>();
        res.addAll(runningPods);
        res.addAll(failedPods);
        res.addAll(successPods);
        return res;
    }

    @Override
    public String toString() {
        return String.format("runningPods:%s, successPods:%s, failedPods:%s", runningPods, successPods, failedPods);
    }
}
