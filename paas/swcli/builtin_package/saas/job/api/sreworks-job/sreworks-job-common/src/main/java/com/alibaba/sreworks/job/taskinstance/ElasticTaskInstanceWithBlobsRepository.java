package com.alibaba.sreworks.job.taskinstance;

import java.util.List;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElasticTaskInstanceWithBlobsRepository
    extends ElasticsearchRepository<ElasticTaskInstanceWithBlobs, String> {

    ElasticTaskInstanceWithBlobs findFirstById(String id);

    List<ElasticTaskInstanceWithBlobs> findAllByJobInstanceId(String jobInstanceId);

}
