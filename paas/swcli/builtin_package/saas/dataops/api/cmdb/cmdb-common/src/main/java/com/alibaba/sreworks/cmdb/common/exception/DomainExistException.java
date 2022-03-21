package com.alibaba.sreworks.cmdb.common.exception;

public class DomainExistException extends Exception {
    public DomainExistException(String message) {
        super(message);
    }

    public DomainExistException(Throwable cause) {
        super(cause);
    }
}
