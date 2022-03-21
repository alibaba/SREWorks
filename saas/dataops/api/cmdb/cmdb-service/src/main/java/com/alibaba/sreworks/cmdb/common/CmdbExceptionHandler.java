package com.alibaba.sreworks.cmdb.common;


import com.alibaba.sreworks.cmdb.common.exception.*;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.common.base.TeslaResultFactory;
import com.alibaba.tesla.common.base.constant.TeslaStatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import static com.alibaba.sreworks.cmdb.common.CmdbCodeEnum.*;

@Slf4j
@ControllerAdvice
public class CmdbExceptionHandler {

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public TeslaBaseResult httpMessageNotReadableException(HttpMessageNotReadableException e) {
        return TeslaResultFactory.buildResult(TeslaStatusCode.USER_ARG_ERROR, e.getMessage(), "");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public TeslaBaseResult illegalArgumentException(IllegalArgumentException e) {
        log.error("Cmdb Illegal Argument Exception Occurred:", e);
        return TeslaResultFactory.buildResult(TeslaStatusCode.USER_ARG_ERROR, e.getMessage(), "");
    }

    @ExceptionHandler(DomainException.class)
    @ResponseBody
    public TeslaBaseResult domainException(DomainException e) {
        log.error("Cmdb Sw Domain Exception Occurred:", e);
        return TeslaResultFactory.buildResult(DomainErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(DomainRefException.class)
    @ResponseBody
    public TeslaBaseResult domainRefException(DomainRefException e) {
        log.error("Cmdb Sw Domain Ref Exception Occurred:", e);
        return TeslaResultFactory.buildResult(DomainRefErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(DomainExistException.class)
    @ResponseBody
    public TeslaBaseResult domainExistException(DomainExistException e) {
        log.error("Cmdb Sw Domain Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(DomainExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(DomainNotExistException.class)
    @ResponseBody
    public TeslaBaseResult domainNotExistException(DomainNotExistException e) {
        log.error("Cmdb Sw Domain Not Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(DomainNotExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityException.class)
    @ResponseBody
    public TeslaBaseResult entityException(EntityException e) {
        log.error("Cmdb Entity Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityExistException.class)
    @ResponseBody
    public TeslaBaseResult entityExistException(EntityExistException e) {
        log.error("Cmdb Entity Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityNotExistException.class)
    @ResponseBody
    public TeslaBaseResult entityNotExistException(EntityNotExistException e) {
        log.error("Cmdb Entity Not Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityNotExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityFieldException.class)
    @ResponseBody
    public TeslaBaseResult entityFieldException(EntityFieldException e) {
        log.error("Cmdb Entity Field Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityFieldErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityFieldExistException.class)
    @ResponseBody
    public TeslaBaseResult entityFieldExistException(EntityFieldExistException e) {
        log.error("Cmdb Entity Field Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityFieldExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(EntityFieldNotExistException.class)
    @ResponseBody
    public TeslaBaseResult entityFieldNotExistException(EntityFieldNotExistException e) {
        log.error("Cmdb Entity Field Not Exist Exception Occurred:", e);
        return TeslaResultFactory.buildResult(EntityFieldNotExistErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(ParamException.class)
    @ResponseBody
    public TeslaBaseResult paramException(ParamException e) {
        log.error("Cmdb Param Exception Occurred:", e);
        return TeslaResultFactory.buildResult(ParamErr.code, e.getMessage(), "");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public TeslaBaseResult exception(Exception e) {
        log.error("Cmdb Server Exception Occurred:", e);
        return TeslaResultFactory.buildResult(TeslaStatusCode.SERVER_ERROR, e.getMessage(), "");
    }
}
