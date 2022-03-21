package com.alibaba.sreworks.job.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HostUtil {

    public static String LOCAL_HOST;

    static {
        try {
            LOCAL_HOST = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("", e);
        }
    }

}
