package com.alibaba.sreworks.cmdb.common.exception;

public class EntityFieldExistException extends Exception {
    public EntityFieldExistException(String message) {
        super(message);
    }

    public EntityFieldExistException(Throwable cause) {
        super(cause);
    }
}
