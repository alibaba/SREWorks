package com.alibaba.tesla.appmanager.server.storage.impl;

public class BaseStorage {

    /**
     * 最大获取 Object 内容字符串大小限制
     */
    protected final long MAX_GET_OBJECT_CONTENT_LENGTH = 10 * 1024 * 1024;

    protected String endpoint;
    protected String accessKey;
    protected String secretKey;

    public BaseStorage(String endpoint, String accessKey, String secretKey) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }
}
