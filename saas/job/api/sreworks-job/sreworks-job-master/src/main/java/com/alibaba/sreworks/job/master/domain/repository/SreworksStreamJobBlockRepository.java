package com.alibaba.sreworks.job.master.domain.repository;

import com.alibaba.sreworks.job.master.domain.DO.SreworksStreamJobBlock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import javax.transaction.Transactional;
import java.util.List;

public interface SreworksStreamJobBlockRepository
    extends JpaRepository<SreworksStreamJobBlock, Long>, JpaSpecificationExecutor<SreworksStreamJobBlock> {

    SreworksStreamJobBlock findFirstById(Long id);

    Page<SreworksStreamJobBlock> findAll(Pageable pageable);

    List<SreworksStreamJobBlock> findAllByStreamJobId(Long streamJobId);

    @Transactional(rollbackOn = Exception.class)
    int deleteAllByStreamJobId(Long streamJobId);

}
