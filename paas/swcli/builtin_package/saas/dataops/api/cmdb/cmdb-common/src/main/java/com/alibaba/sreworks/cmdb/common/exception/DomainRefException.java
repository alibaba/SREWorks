package com.alibaba.sreworks.cmdb.common.exception;

public class DomainRefException extends Exception {
    public DomainRefException(String message) {
        super(message);
    }

    public DomainRefException(Throwable cause) {
        super(cause);
    }
}
