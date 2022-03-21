package com.alibaba.sreworks.cmdb.common.exception;

public class EntityException extends Exception {
    public EntityException(String message) {
        super(message);
    }

    public EntityException(Throwable cause) {
        super(cause);
    }
}
