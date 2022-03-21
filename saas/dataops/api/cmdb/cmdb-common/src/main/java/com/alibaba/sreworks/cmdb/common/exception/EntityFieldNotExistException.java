package com.alibaba.sreworks.cmdb.common.exception;

public class EntityFieldNotExistException extends Exception {
    public EntityFieldNotExistException(String message) {
        super(message);
    }

    public EntityFieldNotExistException(Throwable cause) {
        super(cause);
    }
}
