package com.alibaba.sreworks.cmdb.domain;

import com.alibaba.sreworks.cmdb.domain.SwModel;
import com.alibaba.sreworks.cmdb.domain.SwModelExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface SwModelMapper {
    long countByExample(SwModelExample example);

    int deleteByExample(SwModelExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SwModel record);

    int insertSelective(SwModel record);

    List<SwModel> selectByExampleWithBLOBsWithRowbounds(SwModelExample example, RowBounds rowBounds);

    List<SwModel> selectByExampleWithBLOBs(SwModelExample example);

    List<SwModel> selectByExampleWithRowbounds(SwModelExample example, RowBounds rowBounds);

    List<SwModel> selectByExample(SwModelExample example);

    SwModel selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SwModel record, @Param("example") SwModelExample example);

    int updateByExampleWithBLOBs(@Param("record") SwModel record, @Param("example") SwModelExample example);

    int updateByExample(@Param("record") SwModel record, @Param("example") SwModelExample example);

    int updateByPrimaryKeySelective(SwModel record);

    int updateByPrimaryKeyWithBLOBs(SwModel record);

    int updateByPrimaryKey(SwModel record);
}