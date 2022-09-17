package com.alibaba.tesla.appmanager.dynamicscript.util;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.core.ScriptIdentifier;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * Groovy 工具类
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public class GroovyUtil {

    /**
     * 从 clazz 中获取 Script 标识信息 (kind/name/revision)
     *
     * @param clazz Class
     * @return Script 标识信息
     * @throws IllegalAccessException
     */
    public static ScriptIdentifier getScriptIdentifierFromClass(Class<?> clazz) throws IllegalAccessException {
        String kind = null, name = null;
        Integer revision = null;
        for (Field f : clazz.getFields()) {
            if (f.getType().equals(String.class) || f.getType().equals(Integer.class)) {
                String key = f.getName();
                Object value = f.get(null);
                switch (key) {
                    case "KIND":
                        kind = (String) value;
                        break;
                    case "NAME":
                        name = (String) value;
                        break;
                    case "REVISION":
                        revision = (Integer) value;
                        break;
                    default:
                        break;
                }
            }
        }
        if (StringUtils.isAnyEmpty(kind, name) || revision == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "cannot find kind/name/revision in class");
        }
        return ScriptIdentifier.builder()
                .kind(kind)
                .name(name)
                .revision(revision)
                .build();
    }
}
