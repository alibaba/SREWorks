package com.alibaba.sreworks.dataset.domain.primary;

import com.alibaba.sreworks.dataset.domain.primary.DataModelConfig;
import com.alibaba.sreworks.dataset.domain.primary.DataModelConfigExample;
import com.alibaba.sreworks.dataset.domain.primary.DataModelConfigWithBLOBs;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface DataModelConfigMapper {
    long countByExample(DataModelConfigExample example);

    int deleteByExample(DataModelConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(DataModelConfigWithBLOBs record);

    int insertSelective(DataModelConfigWithBLOBs record);

    List<DataModelConfigWithBLOBs> selectByExampleWithBLOBsWithRowbounds(DataModelConfigExample example, RowBounds rowBounds);

    List<DataModelConfigWithBLOBs> selectByExampleWithBLOBs(DataModelConfigExample example);

    List<DataModelConfig> selectByExampleWithRowbounds(DataModelConfigExample example, RowBounds rowBounds);

    List<DataModelConfig> selectByExample(DataModelConfigExample example);

    DataModelConfigWithBLOBs selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") DataModelConfigWithBLOBs record, @Param("example") DataModelConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") DataModelConfigWithBLOBs record, @Param("example") DataModelConfigExample example);

    int updateByExample(@Param("record") DataModelConfig record, @Param("example") DataModelConfigExample example);

    int updateByPrimaryKeySelective(DataModelConfigWithBLOBs record);

    int updateByPrimaryKeyWithBLOBs(DataModelConfigWithBLOBs record);

    int updateByPrimaryKey(DataModelConfig record);
}