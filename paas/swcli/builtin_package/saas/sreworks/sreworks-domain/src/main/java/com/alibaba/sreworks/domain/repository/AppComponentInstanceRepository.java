package com.alibaba.sreworks.domain.repository;

import java.util.List;

import javax.transaction.Transactional;

import com.alibaba.sreworks.domain.DO.AppComponentInstance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;

/**
 * @author jinghua.yjh
 */
public interface AppComponentInstanceRepository
    extends JpaRepository<AppComponentInstance, Long>, JpaSpecificationExecutor<AppComponentInstance> {

    AppComponentInstance findFirstById(Long id);

    List<AppComponentInstance> findAllByAppInstanceId(Long appInstanceId);

    @Transactional(rollbackOn = Exception.class)
    @Modifying
    void deleteAllByAppInstanceId(Long appInstanceId);

}
