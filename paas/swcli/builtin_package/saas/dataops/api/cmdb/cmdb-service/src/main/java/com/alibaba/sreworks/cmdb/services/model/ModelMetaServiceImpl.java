package com.alibaba.sreworks.cmdb.services.model;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.sreworks.cmdb.api.model.ModelMetaService;
import com.alibaba.sreworks.cmdb.common.constant.DWConstant;
import com.alibaba.sreworks.cmdb.common.exception.DomainNotExistException;
import com.alibaba.sreworks.cmdb.common.exception.ModelExistException;
import com.alibaba.sreworks.cmdb.common.exception.ModelFieldException;
import com.alibaba.sreworks.cmdb.common.exception.ModelNotExistException;
import com.alibaba.sreworks.cmdb.domain.*;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelBaseReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelFieldBaseReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelFieldCreateReq;
import com.alibaba.sreworks.cmdb.operator.ESIndexOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型元管理Service
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2021/11/17 17:20
 */
@Slf4j
@Service
public class ModelMetaServiceImpl implements ModelMetaService {

    @Autowired
    SwDomainMapper domainMapper;

    @Autowired
    SwModelMapper modelMapper;

    @Autowired
    SwModelFieldMapper modelFieldMapper;

    @Autowired
    ESIndexOperator esIndexOperator;

    @Override
    public JSONObject getModelById(Long id) {
        SwModel entity = modelMapper.selectByPrimaryKey(id);
        return richModelDomainInfo(entity);
    }

    @Override
    public JSONObject getModelByName(String name) {
        SwModelExample example =  new SwModelExample();
        example.createCriteria().andNameEqualTo(name);
        List<SwModel> entities = modelMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return convertToJSONObject(null);
        } else {
            return richModelDomainInfo(entities.get(0));
        }
    }

    @Override
    public List<JSONObject> getModels() {
        List<SwModel> entities = modelMapper.selectByExample(new SwModelExample());
        return richEntitiesDomainInfo(entities);
    }

    @Override
    public List<JSONObject> getFieldsByModelId(Long entityId) throws Exception {
        SwModel entity = modelMapper.selectByPrimaryKey(entityId);
        if (entity == null) {
            throw new ModelNotExistException(String.format("模型[id:%s]不存在", entityId));
        }
        return convertToJSONObjects(getFieldsByModelIdNoCheck(entityId));
    }

    @Override
    public List<JSONObject> getFieldsByModelName(String entityName) throws Exception {
        SwModelExample entityExample =  new SwModelExample();
        entityExample.createCriteria().andNameEqualTo(entityName);
        List<SwModel> entities = modelMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            throw new ModelNotExistException(String.format("模型[name:%s]不存在", entityName));
        }
        List<SwModelField> fields = getFieldsByModelIdNoCheck(entities.get(0).getId());
        return convertToJSONObjects(fields);
    }

    @Override
    public JSONObject getModelWithFieldsById(Long id) {
        SwModel entity = modelMapper.selectByPrimaryKey(id);
        if (entity == null) {
            return convertToJSONObject(null);
        }
        JSONObject result = richModelDomainInfo(entity);
        result.put("fields", convertToJSONObjects(getFieldsByModelIdNoCheck(id)));

        return result;
    }

    @Override
    public JSONObject getModelWithFieldsByName(String name) {
        SwModelExample entityExample =  new SwModelExample();
        entityExample.createCriteria().andNameEqualTo(name);
        List<SwModel> entities = modelMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            return convertToJSONObject(null);
        }
        SwModel swModel = entities.get(0);
        JSONObject result = richModelDomainInfo(swModel);
        result.put("fields", convertToJSONObjects(getFieldsByModelIdNoCheck(swModel.getId())));

        return result;
    }

    private List<SwModelField> getFieldsByModelIdNoCheck(Long entityId) {
        SwModelFieldExample example = new SwModelFieldExample();
        example.createCriteria().andModelIdEqualTo(entityId);
        return modelFieldMapper.selectByExample(example);
    }

    private JSONObject richModelDomainInfo(SwModel entity) {
        if (entity == null) {
            return convertToJSONObject(null);
        }

        SwDomain swDomain = getDomainById(entity.getDomainId());

        JSONObject result = convertToJSONObject(entity);
        result.put("domainName", swDomain.getName());
        result.put("domainAbbreviation", swDomain.getAbbreviation());

        return result;
    }

    private List<JSONObject> richEntitiesDomainInfo(List<SwModel> entities) {
        if (CollectionUtils.isEmpty(entities)) {
            return convertToJSONObjects(null);
        }

        List<Integer> domainIds = entities.stream().map(SwModel::getDomainId).collect(Collectors.toList());
        List<SwDomain> swDomains = getDomainsByIds(domainIds);

        Map<Integer, SwDomain> swDomainMaps = new HashMap<>();
        swDomains.forEach(swDomain -> swDomainMaps.put(swDomain.getId(), swDomain));

        return entities.stream().map(entity -> {
            JSONObject result = convertToJSONObject(entity);
            result.put("domainName", swDomainMaps.get(entity.getDomainId()).getName());
            result.put("domainAbbreviation", swDomainMaps.get(entity.getDomainId()).getAbbreviation());
            return result;
        }).collect(Collectors.toList());
    }

    private List<SwDomain> getDomainsByIds(List<Integer> ids) {
        SwDomainExample example = new SwDomainExample();
        example.createCriteria().andIdIn(ids);
        return domainMapper.selectByExample(example);
    }

    private SwDomain getDomainById(Integer id) {
        return domainMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int deleteModelById(Long id) throws Exception {
        SwModel entity = modelMapper.selectByPrimaryKey(id);
        if (entity == null) {
            return 0;
        }

        esIndexOperator.deleteIndex(entity.getTableName());

        SwModelFieldExample example = new SwModelFieldExample();
        example.createCriteria().andModelIdEqualTo(id);
        modelFieldMapper.deleteByExample(example);

        return modelMapper.deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public int deleteModelByName(String name) throws Exception {
        SwModelExample example =  new SwModelExample();
        example.createCriteria().andNameEqualTo(name);
        List<SwModel> entities = modelMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }

        esIndexOperator.deleteIndex(entities.get(0).getTableName());

        SwModelFieldExample fieldExample = new SwModelFieldExample();
        fieldExample.createCriteria().andModelIdEqualTo(entities.get(0).getId());
        modelFieldMapper.deleteByExample(fieldExample);

        return modelMapper.deleteByExample(example);
    }

    @Override
    public int deleteFieldByModelId(Long entityId, String fieldName) throws Exception {
        // 仅仅删除字段定义, 不修改ES存储数据
        if (DWConstant.PARTITION_FIELD.equals(fieldName)) {
            throw new ModelFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwModelFieldExample fieldExample = new SwModelFieldExample();
        fieldExample.createCriteria().andModelIdEqualTo(entityId).andFieldEqualTo(fieldName);
        return modelFieldMapper.deleteByExample(fieldExample);
    }

    @Override
    public int deleteFieldByModelName(String entityName, String fieldName) throws Exception {
        // 仅仅删除字段定义, 不修改ES存储数据
        if (DWConstant.PARTITION_FIELD.equals(fieldName)) {
            throw new ModelFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwModelExample example =  new SwModelExample();
        example.createCriteria().andNameEqualTo(entityName);
        List<SwModel> entities = modelMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(entities)) {
            return 0;
        }

        SwModelFieldExample fieldExample = new SwModelFieldExample();
        fieldExample.createCriteria().andModelIdEqualTo(entities.get(0).getId()).andFieldEqualTo(fieldName);
        return modelFieldMapper.deleteByExample(fieldExample);
    }

    @Override
    @Transactional
    public long createModel(ModelCreateReq req) throws Exception {
        if (!getModelByName(req.getName()).isEmpty()) {
            throw new ModelExistException(String.format("同名模型[%s]已存在", req.getName()));
        }

        SwModel cmdbModel = buildSwModel(req);
        modelMapper.insert(cmdbModel);

        ModelFieldCreateReq fieldCreateReq = new ModelFieldCreateReq();
        fieldCreateReq.setField(DWConstant.PARTITION_FIELD);
        fieldCreateReq.setDim(DWConstant.PARTITION_DIM);
        fieldCreateReq.setType(DWConstant.PARTITION_TYPE);
        fieldCreateReq.setBuildIn(DWConstant.PARTITION_BUILD_IN);
        fieldCreateReq.setAlias(DWConstant.PARTITION_ALIAS);
        fieldCreateReq.setNullable(DWConstant.PARTITION_NULLABLE);
        fieldCreateReq.setDescription(DWConstant.PARTITION_DESCRIPTION);

        SwModelField cmdbModelField = buildSwModelField(cmdbModel.getId(), fieldCreateReq);
        modelFieldMapper.insert(cmdbModelField);

        createTableMeta(cmdbModel.getTableName(), cmdbModel.getTableAlias());

        return cmdbModel.getId();
    }

    @Override
    @Transactional
    public long createModelWithFields(ModelCreateReq req, List<ModelFieldCreateReq> fieldReqs) throws Exception {
        long id = createModel(req);
        for (ModelFieldCreateReq fieldCreateReq : fieldReqs) {
            addFieldByModelId(id, fieldCreateReq);
        }

        return id;
    }

    @Override
    public int addFieldByModelId(Long entityId, ModelFieldCreateReq req) throws Exception {
        if (DWConstant.PARTITION_FIELD.equals(req.getField())) {
            throw new ModelFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwModel entity = modelMapper.selectByPrimaryKey(entityId);
        if (entity == null) {
            throw new ModelNotExistException(String.format("模型[id:%s]不存在", entityId));
        }

        SwModelField cmdbModelField = buildSwModelField(entityId, req);
        return modelFieldMapper.insert(cmdbModelField);
    }

    @Override
    public int addFieldByModelName(String entityName, ModelFieldCreateReq req) throws Exception {
        if (DWConstant.PARTITION_FIELD.equals(req.getField())) {
            throw new ModelFieldException(String.format("默认分区列冲突,%s", DWConstant.PARTITION_FIELD));
        }

        SwModelExample entityExample =  new SwModelExample();
        entityExample.createCriteria().andNameEqualTo(entityName);
        List<SwModel> entities = modelMapper.selectByExample(entityExample);
        if (CollectionUtils.isEmpty(entities)) {
            throw new ModelNotExistException(String.format("模型[name:%s]不存在", entityName));
        }

        SwModelField cmdbModelField = buildSwModelField(entities.get(0).getId(), req);
        return modelFieldMapper.insert(cmdbModelField);
    }

    private SwModel buildSwModel(ModelBaseReq req) throws Exception {
        SwDomain swDomain = domainMapper.selectByPrimaryKey(req.getDomainId());
        if (swDomain == null) {
            throw new DomainNotExistException(String.format("数据域[id:%s]不存在", req.getDomainId()));
        }

        SwModel swModel = new SwModel();
        Date now = new Date();
        swModel.setGmtCreate(now);
        swModel.setGmtModified(now);
        swModel.setName(req.getName());
        swModel.setAlias(req.getAlias());
        swModel.setLayer(req.getLayer());
        swModel.setBuildIn(req.getBuildIn());
        swModel.setPartitionFormat(req.getPartitionFormat());
        swModel.setDomainId(req.getDomainId());
        swModel.setLifecycle(req.getLifecycle());
        swModel.setIcon(req.getIcon());
        swModel.setDescription(req.getDescription());

        String tableAlias = req.getLayer() + "_" + swDomain.getAbbreviation() + "_" + req.getName().toLowerCase();
        swModel.setTableName(tableAlias + "_" + req.getPartitionFormat());
        swModel.setTableAlias(tableAlias);

        return swModel;
    }

    private SwModelField buildSwModelField(Long entityId, ModelFieldBaseReq req) {
        SwModelField swModelField = new SwModelField();
        Date now = new Date();
        swModelField.setGmtCreate(now);
        swModelField.setGmtModified(now);
        swModelField.setField(req.getField());
        swModelField.setAlias(req.getAlias());
        swModelField.setModelId(entityId);
        swModelField.setDim(req.getDim());
        swModelField.setBuildIn(req.getBuildIn());
        swModelField.setType(req.getType().getType());
        swModelField.setNullable(req.getNullable());
        swModelField.setDescription(req.getDescription());

        return swModelField;
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
