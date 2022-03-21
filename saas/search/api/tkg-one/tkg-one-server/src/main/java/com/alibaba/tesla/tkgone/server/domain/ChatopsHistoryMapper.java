package com.alibaba.tesla.tkgone.server.domain;

import com.alibaba.tesla.tkgone.server.domain.ChatopsHistory;
import com.alibaba.tesla.tkgone.server.domain.ChatopsHistoryExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

public interface ChatopsHistoryMapper {
    long countByExample(ChatopsHistoryExample example);

    int deleteByExample(ChatopsHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ChatopsHistory record);

    int insertSelective(ChatopsHistory record);

    List<ChatopsHistory> selectByExampleWithRowbounds(ChatopsHistoryExample example, RowBounds rowBounds);

    List<ChatopsHistory> selectByExample(ChatopsHistoryExample example);

    ChatopsHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ChatopsHistory record, @Param("example") ChatopsHistoryExample example);

    int updateByExample(@Param("record") ChatopsHistory record, @Param("example") ChatopsHistoryExample example);

    int updateByPrimaryKeySelective(ChatopsHistory record);

    int updateByPrimaryKey(ChatopsHistory record);
}