package com.alibaba.sreworks.cmdb.controllers.model;

import com.alibaba.sreworks.cmdb.api.model.ModelMetaService;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelFieldCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.model.ModelWithFieldsCreateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 模型元Control
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2020/12/23 14:55
 */

@Slf4j
@RestController
@RequestMapping("/model/meta/")
@Api(tags = "模型--元数据")
public class ModelMetaControl extends BaseController {

    @Autowired
    ModelMetaService entityMetaService;

    @ApiOperation(value = "查询模型信息(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getModelById", method = RequestMethod.GET)
    public TeslaBaseResult getModelMetaById(@RequestParam(name = "id", defaultValue = "0") Long id) {
        return buildSucceedResult(entityMetaService.getModelById(id));
    }

    @ApiOperation(value = "查询模型信息(根据模型名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "模型名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getModelByName", method = RequestMethod.GET)
    public TeslaBaseResult getModelByName(@RequestParam(name = "name", defaultValue = "0") String name) {
        return buildSucceedResult(entityMetaService.getModelByName(name));
    }

    @ApiOperation(value = "查询模型信息(所有)")
    @RequestMapping(value = "/getModels", method = RequestMethod.GET)
    public TeslaBaseResult getEntities(){
        return buildSucceedResult(entityMetaService.getModels());
    }

    @ApiOperation(value = "查询模型列信息(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getFieldsByModelId", method = RequestMethod.GET)
    public TeslaBaseResult getFieldsByModelId(@RequestParam(name = "id", defaultValue = "0") Long id) throws Exception {
        return buildSucceedResult(entityMetaService.getFieldsByModelId(id));
    }

    @ApiOperation(value = "查询模型列信息(根据模型名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "模型名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getFieldsByModelName", method = RequestMethod.GET)
    public TeslaBaseResult getFieldsByModelName(@RequestParam(name = "name", defaultValue = "0") String name) throws Exception {
        return buildSucceedResult(entityMetaService.getFieldsByModelName(name));
    }

    @ApiOperation(value = "查询模型和列信息(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getModelWithFieldsById", method = RequestMethod.GET)
    public TeslaBaseResult getModelWithFieldsById(@RequestParam(name = "id", defaultValue = "0") Long id) {
        return buildSucceedResult(entityMetaService.getModelWithFieldsById(id));
    }

    @ApiOperation(value = "查询模型和列信息(根据模型名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "模型名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getModelWithFieldsByName", method = RequestMethod.GET)
    public TeslaBaseResult getModelWithFieldsByName(@RequestParam(name = "name", defaultValue = "0") String name) {
        return buildSucceedResult(entityMetaService.getModelWithFieldsByName(name));
    }

    @ApiOperation(value = "删除模型(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "模型ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/deleteModelById", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteModelById(@RequestParam(name = "id", defaultValue = "0") Long id) throws Exception {
        return buildSucceedResult(entityMetaService.deleteModelById(id));
    }

    @ApiOperation(value = "删除模型(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "模型名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteModelByName", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteModelByName(@RequestParam(name = "name", defaultValue = "0") String name) throws Exception {
        return buildSucceedResult(entityMetaService.deleteModelByName(name));
    }

    @ApiOperation(value = "删除模型列(根据模型ID和列名,仅删除列定义)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityId", value = "模型ID", defaultValue = "0", paramType = "query"),
            @ApiImplicitParam(name = "fieldName", value = "列名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteFieldByModelId", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteFieldByModelId(@RequestParam(name = "entityId", defaultValue = "0") Long entityId,
                                                 @RequestParam(name = "fieldName", defaultValue = "xx") String fieldName) throws Exception {
        return buildSucceedResult(entityMetaService.deleteFieldByModelId(entityId, fieldName));
    }

    @ApiOperation(value = "删除模型列(根据模型ID和列名,仅删除列定义)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityName", value = "模型名称", defaultValue = "APP", paramType = "query"),
            @ApiImplicitParam(name = "fieldName", value = "列名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteFieldByModelName", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteFieldByModelName(@RequestParam(name = "entityName", defaultValue = "0") String entityName,
                                                 @RequestParam(name = "fieldName", defaultValue = "xx") String fieldName) throws Exception {
        return buildSucceedResult(entityMetaService.deleteFieldByModelName(entityName, fieldName));
    }


    @ApiOperation(value = "创建模型")
    @RequestMapping(value = "/createModel", method = RequestMethod.POST)
    public TeslaBaseResult createModel(@RequestBody ModelCreateReq req) throws Exception {
        entityMetaService.createModel(req);
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "创建模型(带列信息)")
    @RequestMapping(value = "/createModelWithFields", method = RequestMethod.POST)
    public TeslaBaseResult createModelWithFields(@RequestBody ModelWithFieldsCreateReq req) throws Exception {
        entityMetaService.createModelWithFields(req.getMetaReq(), Arrays.asList(req.getFieldsReq()));
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "新增列(根据模型ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityId", value = "模型ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/addFieldByModelId", method = RequestMethod.POST)
    public TeslaBaseResult addFieldByModelId(@RequestParam(name = "entityId", defaultValue = "0") Long entityId,
                                              @RequestBody @ApiParam(value = "模型列") ModelFieldCreateReq fieldReq) throws Exception {
        entityMetaService.addFieldByModelId(entityId, fieldReq);
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "新增列(根据模型名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityName", value = "模型名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/addFieldByModelName", method = RequestMethod.POST)
    public TeslaBaseResult addFieldByModelName(@RequestParam(name = "entityName", defaultValue = "0") String entityName,
                                              @RequestBody @ApiParam(value = "模型列") ModelFieldCreateReq fieldReq) throws Exception {
        entityMetaService.addFieldByModelName(entityName, fieldReq);
        return buildSucceedResult("done");
    }
}
