package com.alibaba.sreworks.cmdb.controllers.entity;

import com.alibaba.sreworks.cmdb.api.entity.EntityMetaService;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityFieldCreateReq;
import com.alibaba.sreworks.cmdb.domain.req.entity.EntityWithFieldsCreateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 实体元Control
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2020/12/23 14:55
 */

@Slf4j
@RestController
@RequestMapping("/entity/meta/")
@Api(tags = "实体--元数据")
public class EntityMetaControl extends BaseController {

    @Autowired
    EntityMetaService entityMetaService;

    @ApiOperation(value = "查询实体信息(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "实体ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getEntityById", method = RequestMethod.GET)
    public TeslaBaseResult getEntityMetaById(@RequestParam(name = "id", defaultValue = "0") Long id) {
        return buildSucceedResult(entityMetaService.getEntityById(id));
    }

    @ApiOperation(value = "查询实体信息(根据实体名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "实体名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getEntityByName", method = RequestMethod.GET)
    public TeslaBaseResult getEntityByName(@RequestParam(name = "name", defaultValue = "0") String name) {
        return buildSucceedResult(entityMetaService.getEntityByName(name));
    }

    @ApiOperation(value = "查询实体信息(所有)")
    @RequestMapping(value = "/getEntities", method = RequestMethod.GET)
    public TeslaBaseResult getEntities(){
        return buildSucceedResult(entityMetaService.getEntities());
    }

    @ApiOperation(value = "查询实体列信息(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "实体ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getFieldsByEntityId", method = RequestMethod.GET)
    public TeslaBaseResult getFieldsByEntityId(@RequestParam(name = "id", defaultValue = "0") Long id) throws Exception {
        return buildSucceedResult(entityMetaService.getFieldsByEntityId(id));
    }

    @ApiOperation(value = "查询实体列信息(根据实体名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "实体名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getFieldsByEntityName", method = RequestMethod.GET)
    public TeslaBaseResult getFieldsByEntityName(@RequestParam(name = "name", defaultValue = "0") String name) throws Exception {
        return buildSucceedResult(entityMetaService.getFieldsByEntityName(name));
    }

    @ApiOperation(value = "查询实体和列信息(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "实体ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getEntityWithFieldsById", method = RequestMethod.GET)
    public TeslaBaseResult getEntityWithFieldsById(@RequestParam(name = "id", defaultValue = "0") Long id) {
        return buildSucceedResult(entityMetaService.getEntityWithFieldsById(id));
    }

    @ApiOperation(value = "查询实体和列信息(根据实体名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "实体名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getEntityWithFieldsByName", method = RequestMethod.GET)
    public TeslaBaseResult getEntityWithFieldsByName(@RequestParam(name = "name", defaultValue = "0") String name) {
        return buildSucceedResult(entityMetaService.getEntityWithFieldsByName(name));
    }

    @ApiOperation(value = "删除实体(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "实体ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/deleteEntityById", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteEntityById(@RequestParam(name = "id", defaultValue = "0") Long id) throws Exception {
        return buildSucceedResult(entityMetaService.deleteEntityById(id));
    }

    @ApiOperation(value = "删除实体(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "实体名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteEntityByName", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteEntityByName(@RequestParam(name = "name", defaultValue = "0") String name) throws Exception {
        return buildSucceedResult(entityMetaService.deleteEntityByName(name));
    }

    @ApiOperation(value = "删除实体列(根据实体ID和列名,仅删除列定义)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityId", value = "实体ID", defaultValue = "0", paramType = "query"),
            @ApiImplicitParam(name = "fieldName", value = "列名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteFieldByEntityId", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteFieldByEntityId(@RequestParam(name = "entityId", defaultValue = "0") Long entityId,
                                                 @RequestParam(name = "fieldName", defaultValue = "xx") String fieldName) throws Exception {
        return buildSucceedResult(entityMetaService.deleteFieldByEntityId(entityId, fieldName));
    }

    @ApiOperation(value = "删除实体列(根据实体ID和列名,仅删除列定义)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityName", value = "实体名称", defaultValue = "APP", paramType = "query"),
            @ApiImplicitParam(name = "fieldName", value = "列名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/deleteFieldByEntityName", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteFieldByEntityName(@RequestParam(name = "entityName", defaultValue = "0") String entityName,
                                                 @RequestParam(name = "fieldName", defaultValue = "xx") String fieldName) throws Exception {
        return buildSucceedResult(entityMetaService.deleteFieldByEntityName(entityName, fieldName));
    }


    @ApiOperation(value = "创建实体")
    @RequestMapping(value = "/createEntity", method = RequestMethod.POST)
    public TeslaBaseResult createEntity(@RequestBody EntityCreateReq req) throws Exception {
        entityMetaService.createEntity(req);
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "创建实体(带列信息)")
    @RequestMapping(value = "/createEntityWithFields", method = RequestMethod.POST)
    public TeslaBaseResult createEntityWithFields(@RequestBody EntityWithFieldsCreateReq req) throws Exception {
        entityMetaService.createEntityWithFields(req.getMetaReq(), Arrays.asList(req.getFieldsReq()));
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "新增列(根据实体ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityId", value = "实体ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/addFieldByEntityId", method = RequestMethod.POST)
    public TeslaBaseResult addFieldByEntityId(@RequestParam(name = "entityId", defaultValue = "0") Long entityId,
                                              @RequestBody @ApiParam(value = "实体列") EntityFieldCreateReq fieldReq) throws Exception {
        entityMetaService.addFieldByEntityId(entityId, fieldReq);
        return buildSucceedResult("done");
    }

    @ApiOperation(value = "新增列(根据实体名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "entityName", value = "实体名", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/addFieldByEntityName", method = RequestMethod.POST)
    public TeslaBaseResult addFieldByEntityName(@RequestParam(name = "entityName", defaultValue = "0") String entityName,
                                              @RequestBody @ApiParam(value = "实体列") EntityFieldCreateReq fieldReq) throws Exception {
        entityMetaService.addFieldByEntityName(entityName, fieldReq);
        return buildSucceedResult("done");
    }
}
