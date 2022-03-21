package com.alibaba.sreworks.job.master.jobschedule.dag;

import java.util.List;

import com.alibaba.sreworks.job.master.jobschedule.AbstractJobScheduleConf;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DagJobScheduleConf extends AbstractJobScheduleConf {

    private List<DagJobScheduleConfNode> nodes;

    private List<DagJobScheduleConfEdge> edges;

}

