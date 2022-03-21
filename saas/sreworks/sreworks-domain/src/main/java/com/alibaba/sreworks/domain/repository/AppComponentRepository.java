package com.alibaba.sreworks.domain.repository;

import java.util.List;

import javax.transaction.Transactional;

import com.alibaba.sreworks.domain.DO.AppComponent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;

/**
 * @author jinghua.yjh
 */
public interface AppComponentRepository
    extends JpaRepository<AppComponent, Long>, JpaSpecificationExecutor<AppComponent> {

    AppComponent findFirstById(Long id);

    List<AppComponent> findAllByAppId(Long appId);

    List<AppComponent> findAllByNameIn(List<String> nameList);

    @Modifying
    @Transactional(rollbackOn = Exception.class)
    void deleteByAppId(Long appId);

}
