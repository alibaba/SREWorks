package com.alibaba.tesla.tkgone.server.domain;

import com.alibaba.tesla.tkgone.server.domain.Consumer;
import com.alibaba.tesla.tkgone.server.domain.ConsumerExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ConsumerMapper {
    long countByExample(ConsumerExample example);

    int deleteByExample(ConsumerExample example);

    int deleteByPrimaryKey(Long id);

    int insert(Consumer record);

    int insertSelective(Consumer record);

    List<Consumer> selectByExampleWithRowbounds(ConsumerExample example, RowBounds rowBounds);

    List<Consumer> selectByExample(ConsumerExample example);

    Consumer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") Consumer record, @Param("example") ConsumerExample example);

    int updateByExample(@Param("record") Consumer record, @Param("example") ConsumerExample example);

    int updateByPrimaryKeySelective(Consumer record);

    int updateByPrimaryKey(Consumer record);
}