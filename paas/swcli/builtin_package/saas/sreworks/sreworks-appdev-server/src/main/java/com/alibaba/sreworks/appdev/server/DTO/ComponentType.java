package com.alibaba.sreworks.appdev.server.DTO;

public enum ComponentType {

    //作业
    K8S_JOB("作业"),

    //微服务
    K8S_MICROSERVICE("微服务");

    public String cn;

    ComponentType(String cn) {
        this.cn = cn;
    }

}
