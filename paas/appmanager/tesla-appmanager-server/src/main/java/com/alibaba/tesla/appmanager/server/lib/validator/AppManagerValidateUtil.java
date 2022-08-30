package com.alibaba.tesla.appmanager.server.lib.validator;

import com.alibaba.tesla.appmanager.common.util.ObjectConvertUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.Set;

/**
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Slf4j
@Component
public class AppManagerValidateUtil {
    @Autowired
    private Validator validator;

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> validateRes = this.validator.validate(object);
        if (!validateRes.isEmpty()) {
            ConstraintViolationException constraintViolationException = new ConstraintViolationException(validateRes);
            log.warn("invalid params, msg={}||params={}", constraintViolationException.getMessage(), ObjectConvertUtil.toJsonString(object));
            throw constraintViolationException;
        }
    }
}
