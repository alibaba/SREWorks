package com.alibaba.sreworks.cmdb.services.entity;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.cmdb.api.entity.EntityMetaService;
import com.alibaba.sreworks.cmdb.common.constant.DWConstant;
import com.alibaba.sreworks.cmdb.common.exception.EntityExistException;
import com.alibaba.sreworks.cmdb.common.exception.EntityFieldException;
import com.alibaba.sreworks.cmdb.common.exception.EntityNotExistException;
import com.alibaba.sreworks.cmdb.domain.*;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityBaseReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityFieldBaseReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityFieldCreateReq;
import com.alibaba.sreworks.cmdb.operator.ESIndexOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * 实体元管理Service
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/11/17 17:20
 */
@Slf4j
@Service
public class EntityMetaServiceImpl implements EntityMetaService {

    @Autowired
    SwDomainMapper domainMapper;

    @Autowired
    SwEntityMapper entityMapper;

    @Autowired
    SwEntityFieldMapper entityFieldMapper;

    @Autowired
    ESIndexOperator esIndexOperator;

    @Override
    public JSONObject getEntityById(Long id) {
        SwEntity entity = entityMapper.selectByPrimaryKey(id);
        return convertToJSONObject(entity);
    }

    @Override
    public JSONObject getEntityByName(String name) {
        SwEntityExample example =  new SwEntityExample();
        example.createCriteria().andNameEqualTo(name);
        List<SwEntity> entities = entityMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return convertToJSONObject(entities);
        } else {
            return convertToJSONObject(entities.get(0));
        }
    }

    @Override
    public List<JSONObject> getEntities() {
        List<SwEntity> entities = entityMapper.selectByExample(new SwEntityExample());
        return convertToJSONObjects(entities);
    }

    @Override
    public List<JSONObject> getFieldsByEntityId(Long entityId) throws Exception {
        SwEntity entity = entityMapper.selectByPrimaryKey(entityId);
        if (entity == null) {
            throw new EntityNotExistException(String.format("实体[id:%s]不存在", entityId));
        }
        return convertToJSONObjects(getFieldsByEntityIdNoCheck(entityId));
    }

    @Override
    public List<JSONObject> getFieldsByEntityName(String entityName) throws Exception {
        SwEntityExample entityExample =  new SwEntityExample();
        entityExample.createCriteria().andNameEqualTo(entityName);
        List<SwEntity> entities = entityMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            throw new EntityNotExistException(String.format("实体[name:%s]不存在", entityName));
        }
        List<SwEntityField> fields = getFieldsByEntityIdNoCheck(entities.get(0).getId());
        return convertToJSONObjects(fields);
    }

    @Override
    public JSONObject getEntityWithFieldsById(Long id) {
        SwEntity entity = entityMapper.selectByPrimaryKey(id);
        if (entity == null) {
            return convertToJSONObject(null);
        }
        JSONObject result = convertToJSONObject(entity);
        result.put("fields", convertToJSONObjects(getFieldsByEntityIdNoCheck(id)));

        return result;
    }

    @Override
    public JSONObject getEntityWithFieldsByName(String name) {
        SwEntityExample entityExample =  new SwEntityExample();
        entityExample.createCriteria().andNameEqualTo(name);
        List<SwEntity> entities = entityMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            return convertToJSONObject(null);
        }
        SwEntity swEntity = entities.get(0);
        JSONObject result = convertToJSONObject(swEntity);
        result.put("fields", convertToJSONObjects(getFieldsByEntityIdNoCheck(swEntity.getId())));

        return result;
    }

    private List<SwEntityField> getFieldsByEntityIdNoCheck(Long entityId) {
        SwEntityFieldExample example = new SwEntityFieldExample();
        example.createCriteria().andEntityIdEqualTo(entityId);
        return entityFieldMapper.selectByExampleWithBLOBs(example);
    }

    @Override
    @Transactional
    public int deleteEntityById(Long id) throws Exception {
        SwEntity entity = entityMapper.selectByPrimaryKey(id);
        if (entity == null) {
            return 0;
        }

        esIndexOperator.deleteIndex(entity.getTableName());

        SwEntityFieldExample example = new SwEntityFieldExample();
        example.createCriteria().andEntityIdEqualTo(id);
        entityFieldMapper.deleteByExample(example);

        return entityMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int deleteEntityByName(String name) throws Exception {
        SwEntityExample example =  new SwEntityExample();
        example.createCriteria().andNameEqualTo(name);
        List<SwEntity> entities = entityMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }

        esIndexOperator.deleteIndex(entities.get(0).getTableName());

        SwEntityFieldExample fieldExample = new SwEntityFieldExample();
        fieldExample.createCriteria().andEntityIdEqualTo(entities.get(0).getId());
        entityFieldMapper.deleteByExample(fieldExample);

        return entityMapper.deleteByExample(example);
    }

    @Override
    public int deleteFieldByEntityId(Long entityId, String fieldName) throws Exception {
        // 仅仅删除字段定义, 不修改ES存储数据
        if (DWConstant.PARTITION_FIELD.equals(fieldName)) {
            throw new EntityFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwEntityFieldExample fieldExample = new SwEntityFieldExample();
        fieldExample.createCriteria().andEntityIdEqualTo(entityId).andFieldEqualTo(fieldName);
        return entityFieldMapper.deleteByExample(fieldExample);
    }

    @Override
    public int deleteFieldByEntityName(String entityName, String fieldName) throws Exception {
        // 仅仅删除字段定义, 不修改ES存储数据
        if (DWConstant.PARTITION_FIELD.equals(fieldName)) {
            throw new EntityFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwEntityExample example =  new SwEntityExample();
        example.createCriteria().andNameEqualTo(entityName);
        List<SwEntity> entities = entityMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }

        SwEntityFieldExample fieldExample = new SwEntityFieldExample();
        fieldExample.createCriteria().andEntityIdEqualTo(entities.get(0).getId()).andFieldEqualTo(fieldName);
        return entityFieldMapper.deleteByExample(fieldExample);
    }

    @Override
    @Transactional
    public long createEntity(EntityCreateReq req) throws Exception {
        if (!getEntityByName(req.getName()).isEmpty()) {
            throw new EntityExistException(String.format("同名实体[%s]已存在", req.getName()));
        }

        SwEntity cmdbEntity = buildSwEntity(req);
        entityMapper.insert(cmdbEntity);

        EntityFieldCreateReq fieldCreateReq = new EntityFieldCreateReq();
        fieldCreateReq.setField(DWConstant.PARTITION_FIELD);
        fieldCreateReq.setDim(DWConstant.PARTITION_DIM);
        fieldCreateReq.setType(DWConstant.PARTITION_TYPE);
        fieldCreateReq.setBuildIn(DWConstant.PARTITION_BUILD_IN);
        fieldCreateReq.setAlias(DWConstant.PARTITION_ALIAS);
        fieldCreateReq.setNullable(DWConstant.PARTITION_NULLABLE);
        fieldCreateReq.setDescription(DWConstant.PARTITION_DESCRIPTION);

        SwEntityField cmdbEntityField = buildSwEntityField(cmdbEntity.getId(), fieldCreateReq);
        entityFieldMapper.insert(cmdbEntityField);

        createTableMeta(cmdbEntity.getTableName(), cmdbEntity.getTableAlias());

        return cmdbEntity.getId();
    }

    @Override
    @Transactional
    public long createEntityWithFields(EntityCreateReq req, List<EntityFieldCreateReq> fieldReqs) throws Exception {
        long id = createEntity(req);
        for (EntityFieldCreateReq fieldCreateReq : fieldReqs) {
            addFieldByEntityId(id, fieldCreateReq);
        }

        return id;
    }

    @Override
    public int addFieldByEntityId(Long entityId, EntityFieldCreateReq req) throws Exception {
        if (DWConstant.PARTITION_FIELD.equals(req.getField())) {
            throw new EntityFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwEntity entity = entityMapper.selectByPrimaryKey(entityId);
        if (entity == null) {
            throw new EntityNotExistException(String.format("实体[id:%s]不存在", entityId));
        }

        SwEntityField cmdbEntityField = buildSwEntityField(entityId, req);
        return entityFieldMapper.insert(cmdbEntityField);
    }

    @Override
    public int addFieldByEntityName(String entityName, EntityFieldCreateReq req) throws Exception {
        if (DWConstant.PARTITION_FIELD.equals(req.getField())) {
            throw new EntityFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwEntityExample entityExample =  new SwEntityExample();
        entityExample.createCriteria().andNameEqualTo(entityName);
        List<SwEntity> entities = entityMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            throw new EntityNotExistException(String.format("实体[name:%s]不存在", entityName));
        }

        SwEntityField cmdbEntityField = buildSwEntityField(entities.get(0).getId(), req);
        return entityFieldMapper.insert(cmdbEntityField);
    }

    private SwEntity buildSwEntity(EntityBaseReq req) {
        SwEntity swEntity = new SwEntity();
        Date now = new Date();
        swEntity.setGmtCreate(now);
        swEntity.setGmtModified(now);
        swEntity.setName(req.getName());
        swEntity.setAlias(req.getAlias());
        swEntity.setLayer(req.getLayer());
        swEntity.setBuildIn(req.getBuildIn());
        swEntity.setPartitionFormat(req.getPartitionFormat());
        swEntity.setLifecycle(req.getLifecycle());
        swEntity.setIcon(req.getIcon());
        swEntity.setDescription(req.getDescription());

        String tableAlias = req.getLayer()+ "_" + req.getName().toLowerCase();
        swEntity.setTableName(tableAlias + "_" + req.getPartitionFormat());
        swEntity.setTableAlias(tableAlias);

        return swEntity;
    }

    private SwEntityField buildSwEntityField(Long entityId, EntityFieldBaseReq req) {
        SwEntityField swEntityField = new SwEntityField();
        Date now = new Date();
        swEntityField.setGmtCreate(now);
        swEntityField.setGmtModified(now);
        swEntityField.setField(req.getField());
        swEntityField.setAlias(req.getAlias());
        swEntityField.setEntityId(entityId);
        swEntityField.setDim(req.getDim());
        swEntityField.setBuildIn(req.getBuildIn());
        swEntityField.setType(req.getType().getType());
        swEntityField.setNullable(req.getNullable());
        swEntityField.setDescription(req.getDescription());

        return swEntityField;
    }

    private void createTableMeta(String tableName, String tableAlias) throws Exception {
        // TODO 按照数据类型做mapping映射
        String index = "<" + tableName + ">";
        esIndexOperator.createIndexIfNotExist(index, tableAlias);
    }

    private void addTableField() {
        // TODO 按照数据类型做mapping映射
    }
}
