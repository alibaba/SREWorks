package com.alibaba.sreworks.cmdb.domain;

import com.alibaba.sreworks.cmdb.domain.SwDomain;
import com.alibaba.sreworks.cmdb.domain.SwDomainExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface SwDomainMapper {
    long countByExample(SwDomainExample example);

    int deleteByExample(SwDomainExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(SwDomain record);

    int insertSelective(SwDomain record);

    List<SwDomain> selectByExampleWithBLOBsWithRowbounds(SwDomainExample example, RowBounds rowBounds);

    List<SwDomain> selectByExampleWithBLOBs(SwDomainExample example);

    List<SwDomain> selectByExampleWithRowbounds(SwDomainExample example, RowBounds rowBounds);

    List<SwDomain> selectByExample(SwDomainExample example);

    SwDomain selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") SwDomain record, @Param("example") SwDomainExample example);

    int updateByExampleWithBLOBs(@Param("record") SwDomain record, @Param("example") SwDomainExample example);

    int updateByExample(@Param("record") SwDomain record, @Param("example") SwDomainExample example);

    int updateByPrimaryKeySelective(SwDomain record);

    int updateByPrimaryKeyWithBLOBs(SwDomain record);

    int updateByPrimaryKey(SwDomain record);
}