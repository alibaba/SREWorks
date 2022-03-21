package com.alibaba.sreworks.dataset.domain.primary;

import com.alibaba.sreworks.dataset.domain.primary.DataInterfaceParams;
import com.alibaba.sreworks.dataset.domain.primary.DataInterfaceParamsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface DataInterfaceParamsMapper {
    long countByExample(DataInterfaceParamsExample example);

    int deleteByExample(DataInterfaceParamsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataInterfaceParams record);

    int insertSelective(DataInterfaceParams record);

    List<DataInterfaceParams> selectByExampleWithRowbounds(DataInterfaceParamsExample example, RowBounds rowBounds);

    List<DataInterfaceParams> selectByExample(DataInterfaceParamsExample example);

    DataInterfaceParams selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataInterfaceParams record, @Param("example") DataInterfaceParamsExample example);

    int updateByExample(@Param("record") DataInterfaceParams record, @Param("example") DataInterfaceParamsExample example);

    int updateByPrimaryKeySelective(DataInterfaceParams record);

    int updateByPrimaryKey(DataInterfaceParams record);
}