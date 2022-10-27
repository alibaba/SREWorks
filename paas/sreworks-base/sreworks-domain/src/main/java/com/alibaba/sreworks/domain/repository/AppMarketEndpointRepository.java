package com.alibaba.sreworks.domain.repository;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.domain.DO.AppMarketEndpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface AppMarketEndpointRepository extends JpaRepository<AppMarketEndpoint, Long>, JpaSpecificationExecutor<AppMarketEndpoint> {

    AppMarketEndpoint findFirstById(Long id);

    AppMarketEndpoint findFirstByName(String name);

}
