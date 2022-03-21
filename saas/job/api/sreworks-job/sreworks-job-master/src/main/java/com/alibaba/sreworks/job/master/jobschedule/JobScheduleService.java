package com.alibaba.sreworks.job.master.jobschedule;

import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.job.master.jobschedule.dag.DagJobScheduleConf;
import com.alibaba.sreworks.job.utils.BeansUtil;
import com.alibaba.sreworks.job.utils.JsonUtil;
import com.alibaba.tesla.dag.model.repository.TcDagNodeRepository;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobScheduleService {

    @Autowired
    List<AbstractJobSchedule<? extends AbstractJobScheduleConf>> jobScheduleList;

    @Autowired
    TcDagNodeRepository dagNodeRepository;

    public List<String> listType() {
        return jobScheduleList.stream().map(AbstractJobSchedule::getType).collect(Collectors.toList());
    }

    public AbstractJobSchedule<? extends AbstractJobScheduleConf> getJobSchedule(String type) throws Exception {
        for (AbstractJobSchedule<? extends AbstractJobScheduleConf> jobSchedule : jobScheduleList) {
            if (type.equals(jobSchedule.getType())) {
                return jobSchedule;
            }
        }
        throw new Exception("can not find schedule by type: " + type);
    }

}
