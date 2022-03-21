package com.alibaba.sreworks.job.master.domain.repository;

import java.util.List;

import javax.transaction.Transactional;

import com.alibaba.sreworks.job.master.domain.DO.SreworksJobWorker;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author jinghua.yjh
 */
public interface SreworksJobWorkerRepository
    extends JpaRepository<SreworksJobWorker, Long>, JpaSpecificationExecutor<SreworksJobWorker> {

    SreworksJobWorker findFirstByAddress(String address);

    List<SreworksJobWorker> findAllByEnable(Integer enable);

    List<SreworksJobWorker> findAllByExecTypeListLike(String execType);

    @Transactional(rollbackOn = Exception.class)
    void deleteAllByGmtModifiedBefore(Long minGmtModified);

}
