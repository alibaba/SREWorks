package com.elasticsearch.cloud.monitor.metric.alarm.blink.minio;

import com.elasticsearch.cloud.monitor.metric.common.client.MinioConfig;
import io.minio.*;
import io.minio.messages.Bucket;
import io.minio.messages.Item;
import io.minio.messages.VersioningConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.List;


/**
 * @author: fangzong.ly
 * @date: 2021/08/30 20:35
 */
public class MinioClientProdTest {
    private  MinioConfig minioConfig;

    @Before
    public void setUp() throws Exception {
        String endpoint = "http://minio.c38cca9c474484bdc9873f44f733d8bcd.cn-beijing.alicontainer.com/";
        String ak = "XmizyTRKhgYTrVkK";
        String sk = "Df229gtwZ4bssMzK23VJXq9vrGqpxdHA";
        String bucket = "vvp";

        minioConfig = new MinioConfig(endpoint, ak, sk, bucket, "");
    }

    @Test
    public void testListBuckets() throws Exception {
        MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
        List<Bucket> buckets = minioClient.listBuckets();
        buckets.forEach(bucket -> {
            System.out.println(bucket.name());
        });
    }

    @Test
    public void testGetObject() throws Exception {
        String file = "test_py3.py";
        String lastVersion="950f78ed-42a1-47c4-8c0c-a9b0273d8cd1";
        MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfig.getBucket()).build());
            if (bucketExists) {
                try {
                    StatObjectResponse objectResponse = minioClient.statObject(StatObjectArgs.builder().bucket(minioConfig.getBucket()).object(file).build());
                    System.out.println(objectResponse);
                    GetObjectResponse streamResponse = minioClient.getObject(GetObjectArgs.builder().bucket(minioConfig.getBucket()).object(file).build());
                    System.out.println(streamResponse.headers());
                    System.out.println(streamResponse.headers().get("x-amz-version-id"));
                    Iterable<Result<Item>> ll = minioClient.listObjects(ListObjectsArgs.builder().bucket(minioConfig.getBucket()).includeVersions(true).build());
                    ll.forEach(lll -> {
                        try {
                            Item item = lll.get();
                            System.out.println(item.objectName());
                            System.out.println(item.versionId());
                        } catch (Exception ex) {

                        }

                    });
                } catch (Exception ex) {
                    System.out.println(ex);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    @Test
    public void testCreateBucket() throws Exception {
        String bucketName = "vvp";
        MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());

        VersioningConfiguration configuration = new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, null);
        minioClient.setBucketVersioning(SetBucketVersioningArgs.builder().bucket(bucketName).config(configuration).build());
    }

    @Test
    public void testCreateObject() throws Exception {
        MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();

        minioClient.uploadObject(UploadObjectArgs.builder().bucket(minioConfig.getBucket()).object("vvp/artifacts/namespaces/default/rules.json").filename("/Users/metric-flink/metric-alarm/src/test/resources/rules.json").build());
    }

    @Test
    public void testDeleteObject() throws Exception {
        MinioClient minioClient = MinioClient.builder().endpoint(minioConfig.getEndpoint()).credentials(minioConfig.getAccessKey(), minioConfig.getSecretKey()).build();
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(minioConfig.getBucket()).object("sreworks").build());
//        minioClient.removeObjects(RemoveObjectsArgs.builder().bucket(minioConfig.getBucket()).build());
//        minioClient.removeBucket(RemoveBucketArgs.builder().bucket(minioConfig.getBucket()).build());
    }
}
