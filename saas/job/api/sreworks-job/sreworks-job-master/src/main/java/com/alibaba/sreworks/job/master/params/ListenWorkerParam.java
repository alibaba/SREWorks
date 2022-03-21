package com.alibaba.sreworks.job.master.params;

import java.util.List;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ListenWorkerParam {

    private String address;

    private List<String> execTypeList;

}
