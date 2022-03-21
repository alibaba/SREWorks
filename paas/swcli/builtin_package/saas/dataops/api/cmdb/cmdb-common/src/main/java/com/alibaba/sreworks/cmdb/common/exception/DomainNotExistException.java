package com.alibaba.sreworks.cmdb.common.exception;

public class DomainNotExistException extends Exception {
    public DomainNotExistException(String message) {
        super(message);
    }

    public DomainNotExistException(Throwable cause) {
        super(cause);
    }
}
