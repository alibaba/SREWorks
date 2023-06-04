package com.alibaba.sreworks.job.master.domain.repository;

import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJob;
import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobRuntime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author jiongen.zje
 */
public interface SreworksStreamJobRuntimeRepository
    extends JpaRepository<SreworksStreamJobRuntime, Long>, JpaSpecificationExecutor<SreworksStreamJob> {

    SreworksStreamJobRuntime findFirstById(Long id);

    Page<SreworksStreamJobRuntime> findAll(Pageable pageable);

}
