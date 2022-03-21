package com.alibaba.sreworks.cmdb.common.exception;

public class DomainException extends Exception {
    public DomainException(String message) {
        super(message);
    }

    public DomainException(Throwable cause) {
        super(cause);
    }
}
