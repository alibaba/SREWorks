package com.alibaba.sreworks.job.master.common;


import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Data
public class JobApplicationProperties {

    @Value("${flink.vvp.endpoint}")
    private String flinkVvpEndpoint;
    
}