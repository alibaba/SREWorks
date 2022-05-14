package com.alibaba.tesla.appmanager.workflow.repository.mapper;

import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDO;
import com.alibaba.tesla.appmanager.workflow.repository.domain.WorkflowTaskDOExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WorkflowTaskDOMapper {
    long countByExample(WorkflowTaskDOExample example);

    int deleteByExample(WorkflowTaskDOExample example);

    int insertSelective(WorkflowTaskDO record);

    List<WorkflowTaskDO> selectByExampleWithBLOBs(WorkflowTaskDOExample example);

    List<WorkflowTaskDO> selectByExample(WorkflowTaskDOExample example);

    int updateByExampleSelective(@Param("record") WorkflowTaskDO record, @Param("example") WorkflowTaskDOExample example);

    int updateByPrimaryKeySelective(WorkflowTaskDO record);
}