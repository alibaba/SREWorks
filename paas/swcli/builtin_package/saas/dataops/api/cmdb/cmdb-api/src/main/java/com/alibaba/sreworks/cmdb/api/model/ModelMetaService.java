package com.alibaba.sreworks.cmdb.api.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.cmdb.api.BasicApi;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelFieldCreateReq;

import java.util.List;

/**
 * 数据模型元信息接口
 */
public interface ModelMetaService extends BasicApi {

    /**
     * 根据模型ID查询模型信息
     * @return
     */
    JSONObject getModelById(Long id);

    /**
     * 根据模型名称查询模型信息
     * @return
     */
    JSONObject getModelByName(String name);

    /**
     * 查询模型列表
     * @return
     */
    List<JSONObject> getModels();

    /**
     * 查询模型列信息
     * @return
     */
    List<JSONObject> getFieldsByModelId(Long entityId) throws Exception;

    /**
     * 查询模型列信息
     * @return
     */
    List<JSONObject> getFieldsByModelName(String entityName) throws Exception;

    /**
     * 查询模型和模型列信息
     * @return
     */
    JSONObject getModelWithFieldsById(Long id);

    /**
     * 查询模型和模型列信息
     * @return
     */
    JSONObject getModelWithFieldsByName(String name);

    /**
     * 根据模型ID删除模型
     * @param id
     * @return
     */
    int deleteModelById(Long id) throws Exception;

    /**
     * 根据模型名称删除模型
     * @param name
     * @return
     */
    int deleteModelByName(String name) throws Exception;

    /**
     * 根据模型ID删除列
     * @param entityId
     * @param fieldName
     * @return
     */
    int deleteFieldByModelId(Long entityId, String fieldName) throws Exception;

    /**
     * 根据模型名称删除列
     * @param entityName
     * @param fieldName
     * @return
     */
    int deleteFieldByModelName(String entityName, String fieldName) throws Exception;

    /**
     * 创建模型
     * @param req
     * @return 模型ID
     */
    long createModel(ModelCreateReq req) throws Exception;

    /**
     * 创建模型(带列)
     * @param req
     * @return 模型ID
     */
    long createModelWithFields(ModelCreateReq req, List<ModelFieldCreateReq> fieldReqs) throws Exception;

    /**
     * 新增模型列
     */
    int addFieldByModelId(Long entityId, ModelFieldCreateReq req) throws Exception;

    /**
     * 新增模型列
     */
    int addFieldByModelName(String entityName, ModelFieldCreateReq req) throws Exception;
}
