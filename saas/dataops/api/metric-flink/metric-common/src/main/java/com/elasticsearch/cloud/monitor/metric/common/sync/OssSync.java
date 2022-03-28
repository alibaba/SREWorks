package com.elasticsearch.cloud.monitor.metric.common.sync;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.google.gson.Gson;
import com.opensearch.cobble.thread.Stoppable;
import com.opensearch.cobble.thread.StoppableThread;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaoping
 * @date 2020/5/2
 */
@Slf4j
public class OssSync<T> {
    private String bucket;
    private String file;
    private OSSClient ossClient;
    private final static Long defaultSyncInterval = TimeUnit.MINUTES.toMillis(1);
    private long syncInterval;
    private volatile long lastSyncTime = 0;
    private volatile OssSyncCallBack<T> callBack;
    private StoppableThread updateThread;
    private Type type;

    public OssSync(String ossEndpoint, String accessKey, String accessSecret, String bucket, String file) {
        this(ossEndpoint, accessKey, accessSecret, bucket, file, defaultSyncInterval);
    }

    public OssSync(String ossEndpoint, String accessKey, String accessSecret, String bucket, String file,
        long syncInterval) {
        this.ossClient = new OSSClient(ossEndpoint, accessKey, accessSecret);
        this.bucket = bucket;
        this.file = file;
        this.syncInterval = syncInterval;
        updateThread = new StoppableThread(new UpdateStoppable());
        updateThread.start();

    }

    public void registerCallBack(OssSyncCallBack<T> callBack, Type type) {
        this.callBack = callBack;
        this.type = type;
    }

    public void close() {
        if (updateThread != null) {
            updateThread.stopRunning();
        }
        if (ossClient != null) {
            ossClient.shutdown();
        }

    }

    public abstract static class OssSyncCallBack<T> {
        public abstract void update(T t) throws InterruptedException;

        public void ossSyncOccur() throws InterruptedException {}
    }

    private void update(String content) throws InterruptedException {
        if (callBack != null) {
            T data = new Gson().fromJson(content, type);
            callBack.update(data);
        }
    }

    public class UpdateStoppable implements Stoppable {

        @Override
        public void stopped(String why, Throwable t) {
            log.error(String.format("oss UpdateStoppable stop because %s", why), t);
        }

        @Override
        public boolean runOnce() throws InterruptedException {
            try {
                doSync();
            } catch (InterruptedException e) {
                throw e;
            } catch (Throwable e) {
                log.error(String.format("syn oss error %s", e.getMessage()), e);
            }
            return false;
        }

        private void doSync() throws IOException, InterruptedException {
            if (callBack != null) {
                String content = readOssFile();
                if (content != null) {
                    update(content);
                }
                callBack.ossSyncOccur();
            }

        }

        private String readOssFile() throws IOException {
            if (!ossClient.doesObjectExist(bucket, file)) {
                log.error(String.format("bucket %s file %s not exist", bucket, file));
                return null;
            }
            ObjectMetadata metadata = ossClient.getObjectMetadata(bucket, file);
            if (metadata.getLastModified().getTime() < lastSyncTime) {
                return null;
            }
            lastSyncTime = System.currentTimeMillis();

            StringBuilder data = new StringBuilder();
            OSSObject ossObject = ossClient.getObject(bucket, file);

            InputStream content = ossObject.getObjectContent();

            if (content != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                while (true) {
                    String line = reader.readLine();
                    if (line == null) {
                        break;
                    }
                    data.append(line);
                }
                content.close();
            }

            return data.toString();
        }

        @Override
        public long getSleepInterval() {
            return syncInterval;
        }
    }
}
