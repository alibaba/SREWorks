package com.alibaba.sreworks.job.master.event;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import lombok.Data;

@Data
public abstract class AbstractJobEventConsumer {

    public JobEventConf conf;

    public abstract List<JSONObject> poll();

    public abstract void close();

}
