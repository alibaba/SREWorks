package com.alibaba.tesla.tkgone.server.domain;

import com.alibaba.tesla.tkgone.server.domain.ConsumerHistory;
import com.alibaba.tesla.tkgone.server.domain.ConsumerHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ConsumerHistoryMapper {
    long countByExample(ConsumerHistoryExample example);

    int deleteByExample(ConsumerHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ConsumerHistory record);

    int insertSelective(ConsumerHistory record);

    List<ConsumerHistory> selectByExampleWithRowbounds(ConsumerHistoryExample example, RowBounds rowBounds);

    List<ConsumerHistory> selectByExample(ConsumerHistoryExample example);

    ConsumerHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ConsumerHistory record, @Param("example") ConsumerHistoryExample example);

    int updateByExample(@Param("record") ConsumerHistory record, @Param("example") ConsumerHistoryExample example);

    int updateByPrimaryKeySelective(ConsumerHistory record);

    int updateByPrimaryKey(ConsumerHistory record);
}