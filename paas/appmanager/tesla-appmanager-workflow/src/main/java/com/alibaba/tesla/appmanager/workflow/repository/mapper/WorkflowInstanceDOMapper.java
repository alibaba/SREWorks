package com.alibaba.tesla.appmanager.workflow.repository.mapper;

import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowInstanceDOExample;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface WorkflowInstanceDOMapper {
    long countByExample(WorkflowInstanceDOExample example);

    int deleteByExample(WorkflowInstanceDOExample example);

    int insertSelective(WorkflowInstanceDO record);

    List<WorkflowInstanceDO> selectByExampleWithBLOBs(WorkflowInstanceDOExample example);

    List<WorkflowInstanceDO> selectByExample(WorkflowInstanceDOExample example);

    int updateByExampleSelective(@Param("record") WorkflowInstanceDO record, @Param("example") WorkflowInstanceDOExample example);

    int updateByPrimaryKeySelective(WorkflowInstanceDO record);
}