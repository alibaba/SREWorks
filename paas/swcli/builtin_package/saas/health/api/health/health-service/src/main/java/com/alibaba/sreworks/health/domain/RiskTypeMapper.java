package com.alibaba.sreworks.health.domain;

import com.alibaba.sreworks.health.domain.RiskType;
import com.alibaba.sreworks.health.domain.RiskTypeExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface RiskTypeMapper {
    long countByExample(RiskTypeExample example);

    int deleteByExample(RiskTypeExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(RiskType record);

    int insertSelective(RiskType record);

    List<RiskType> selectByExampleWithRowbounds(RiskTypeExample example, RowBounds rowBounds);

    List<RiskType> selectByExample(RiskTypeExample example);

    RiskType selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") RiskType record, @Param("example") RiskTypeExample example);

    int updateByExample(@Param("record") RiskType record, @Param("example") RiskTypeExample example);

    int updateByPrimaryKeySelective(RiskType record);

    int updateByPrimaryKey(RiskType record);
}