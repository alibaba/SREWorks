package com.alibaba.tesla.appmanager.server.repository.mapper;

import com.alibaba.tesla.appmanager.server.repository.domain.AppAddonDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppAddonExample;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author qiuqiang.qq@alibaba-inc.com
 */
@Mapper
public interface AppAddonMapper {
    long countByExample(AppAddonExample example);

    int deleteByExample(AppAddonExample example);

    /**
     * delete by primary key
     *
     * @param id primaryKey
     * @return deleteCount
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert record to table
     *
     * @param record the record
     * @return insert count
     */
    int insert(AppAddonDO record);

    /**
     * insert record to table selective
     *
     * @param record the record
     * @return insert count
     */
    int insertSelective(AppAddonDO record);

    List<AppAddonDO> selectByExample(AppAddonExample example);

    /**
     * select by primary key
     *
     * @param id primary key
     * @return object by primary key
     */
    AppAddonDO selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AppAddonDO record, @Param("example") AppAddonExample example);

    int updateByExample(@Param("record") AppAddonDO record, @Param("example") AppAddonExample example);

    /**
     * update record selective
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKeySelective(AppAddonDO record);

    /**
     * update record
     *
     * @param record the updated record
     * @return update count
     */
    int updateByPrimaryKey(AppAddonDO record);
}