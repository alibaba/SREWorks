package com.alibaba.tesla.appmanager.common.service;

import org.slf4j.Logger;

public interface StreamLogService {

    void info(String streamKey, String msg);

    void info(String streamKey, String msg, Logger logger);

    void clean(String streamKey);

    void clean(String streamKey, boolean storeLog);

    void clean(String streamKey, String msg, boolean storeLog);

    void clean(String streamKey, String msg, Logger logger, boolean storeLog);
}
