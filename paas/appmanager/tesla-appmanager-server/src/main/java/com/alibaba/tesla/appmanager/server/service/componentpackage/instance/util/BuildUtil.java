package com.alibaba.tesla.appmanager.server.service.componentpackage.instance.util;

/**
 * @ClassName: BuildUtil
 * @Author: dyj
 * @DATE: 2022-08-31
 * @Description:
 **/
public class BuildUtil {
    public static String genLogContent(String pod, String message){
        String logContent = String.format("\n====container:%s====\n-log:%s", pod, message);
        return logContent;
    }
}
