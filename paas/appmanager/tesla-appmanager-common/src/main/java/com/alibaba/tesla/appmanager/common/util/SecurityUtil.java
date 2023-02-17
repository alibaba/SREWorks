package com.alibaba.tesla.appmanager.common.util;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import org.apache.commons.lang3.StringUtils;

/**
 * 安全工具类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class SecurityUtil {

    /**
     * 非法字符
     */
    public static final String INVALID_CHARACTERS = ";'$(){}|&`\"";

    /**
     * 检查用户输入，是否包含特殊字符
     *
     * @param input 用户输入内容
     */
    public static void checkInput(String input) {
        if (StringUtils.isEmpty(input)) {
            return;
        }

        for (int i = 0; i < INVALID_CHARACTERS.length(); i++) {
            if (input.contains(String.valueOf(INVALID_CHARACTERS.charAt(i)))) {
                throw new AppException(AppErrorCode.SECURITY_ERROR, "unsafe characters found in user input");
            }
        }
    }
}
