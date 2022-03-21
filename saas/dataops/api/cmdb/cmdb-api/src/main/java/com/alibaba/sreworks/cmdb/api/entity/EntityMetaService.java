package com.alibaba.sreworks.cmdb.api.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.cmdb.api.BasicApi;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityFieldCreateReq;

import java.util.List;

/**
 * 数据实体元信息接口
 */
public interface EntityMetaService extends BasicApi {

    /**
     * 根据实体ID查询实体信息
     * @return
     */
    JSONObject getEntityById(Long id);

    /**
     * 根据实体名称查询实体信息
     * @return
     */
    JSONObject getEntityByName(String name);

    /**
     * 查询实体列表
     * @return
     */
    List<JSONObject> getEntities();

    /**
     * 查询实体列信息
     * @return
     */
    List<JSONObject> getFieldsByEntityId(Long entityId) throws Exception;

    /**
     * 查询实体列信息
     * @return
     */
    List<JSONObject> getFieldsByEntityName(String entityName) throws Exception;

    /**
     * 查询实体和实体列信息
     * @return
     */
    JSONObject getEntityWithFieldsById(Long id);

    /**
     * 查询实体和实体列信息
     * @return
     */
    JSONObject getEntityWithFieldsByName(String name);

    /**
     * 根据实体ID删除实体
     * @param id
     * @return
     */
    int deleteEntityById(Long id) throws Exception;

    /**
     * 根据实体名称删除实体
     * @param name
     * @return
     */
    int deleteEntityByName(String name) throws Exception;

    /**
     * 根据实体ID删除列
     * @param entityId
     * @param fieldName
     * @return
     */
    int deleteFieldByEntityId(Long entityId, String fieldName) throws Exception;

    /**
     * 根据实体名称删除列
     * @param entityName
     * @param fieldName
     * @return
     */
    int deleteFieldByEntityName(String entityName, String fieldName) throws Exception;

    /**
     * 创建实体
     * @param req
     * @return 实体ID
     */
    long createEntity(EntityCreateReq req) throws Exception;

    /**
     * 创建实体(带列)
     * @param req
     * @return 实体ID
     */
    long createEntityWithFields(EntityCreateReq req, List<EntityFieldCreateReq> fieldReqs) throws Exception;

    /**
     * 新增实体列
     */
    int addFieldByEntityId(Long entityId, EntityFieldCreateReq req) throws Exception;

    /**
     * 新增实体列
     */
    int addFieldByEntityName(String entityName, EntityFieldCreateReq req) throws Exception;
}
