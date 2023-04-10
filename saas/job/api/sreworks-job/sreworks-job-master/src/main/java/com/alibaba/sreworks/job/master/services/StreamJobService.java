package com.alibaba.sreworks.job.master.services;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.common.JobTriggerType;
import com.alibaba.sreworks.job.master.domain.DO.ElasticJobInstance;
import com.alibaba.sreworks.job.master.domain.DO.SreworksJob;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import com.alibaba.sreworks.job.master.domain.DTO.JobInstanceStatus;
import com.alibaba.sreworks.job.master.domain.DTO.SreworksJobDTO;
import com.alibaba.sreworks.job.master.domain.DTO.SreworksStreamJobDTO;
import com.alibaba.sreworks.job.master.domain.repository.ElasticJobInstanceRepository;
import com.alibaba.sreworks.job.master.domain.repository.SreworksJobRepository;
import com.alibaba.sreworks.job.master.domain.repository.SreworksJobTaskRepository;
import com.alibaba.sreworks.job.master.domain.repository.SreworksStreamJobRepository;
import com.alibaba.sreworks.job.master.jobscene.JobSceneService;
import com.alibaba.sreworks.job.master.jobschedule.JobScheduleService;
import com.alibaba.sreworks.job.master.jobtrigger.JobTriggerService;
import com.alibaba.sreworks.job.master.params.JobCreateParam;
import com.alibaba.sreworks.job.master.params.JobEditScheduleParam;
import com.alibaba.sreworks.job.master.params.JobModifyParam;
import com.alibaba.sreworks.job.master.params.StreamJobCreateParam;
import com.alibaba.sreworks.job.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StreamJobService {

    @Autowired
    SreworksStreamJobRepository streamJobRepository;


    public Page<SreworksStreamJobDTO> gets(Pageable pageable) throws Exception {
        Page<SreworksStreamJob> jobs = streamJobRepository.findAll(pageable);

        return jobs.map(streamJob -> {
            SreworksStreamJobDTO jobDTO = new SreworksStreamJobDTO(streamJob);
            BeanUtils.copyProperties(streamJob, jobDTO);
            return jobDTO;
        });
    }

//    public SreworksJobDTO get(Long id) {
////        SreworksJob job = jobRepository.findFirstById(id);
////        if (job == null) {
////            throw new Exception("id is not exists");
////        }
////        SreworksJobDTO jobDTO = new SreworksJobDTO(job);
////        if (!StringUtil.isEmpty(job.getSceneType())) {
////            jobDTO.setSceneConf(jobSceneService.getJobScene(job.getSceneType()).getConf(job.getId()));
////        }
////        if (!StringUtil.isEmpty(job.getScheduleType())) {
////            jobDTO.setScheduleConf(jobScheduleService.getJobSchedule(job.getScheduleType()).getConf(job.getId()));
////        }
////        if (!StringUtil.isEmpty(job.getTriggerType()) && job.getTriggerType().equals(JobTriggerType.CRON.getType())) {
////            JSONObject triggerConf = jobDTO.getTriggerConf();
////            triggerConf.put("enabled", jobTriggerService.getJobTrigger(job.getTriggerType()).getState(id));
////            jobDTO.setTriggerConf(triggerConf);
////        }
////        return jobDTO;
//    }

    public SreworksStreamJob create(StreamJobCreateParam param) throws Exception {
        SreworksStreamJob job = param.job();
        job = streamJobRepository.saveAndFlush(job);
        return job;
    }


    private void deleteJob(SreworksJob job) throws Exception {
//        if (job.getSceneType() != null) {
//            jobSceneService.getJobScene(job.getSceneType()).delete(job.getId());
//        }
//        if (job.getScheduleType() != null) {
//            jobScheduleService.getJobSchedule(job.getScheduleType()).delete(job.getId());
//        }
//        if (job.getTriggerType() != null) {
//            jobTriggerService.getJobTrigger(job.getTriggerType()).delete(job.getId());
//        }
//        jobRepository.deleteById(job.getId());
    }

    public void modify(Long id, JobModifyParam param) throws Exception {

    }


}
