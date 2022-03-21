package com.alibaba.sreworks.cmdb.controllers.domain;

import com.alibaba.sreworks.cmdb.api.domain.DomainService;
import com.alibaba.sreworks.cmdb.domain.req.domain.DomainCreateReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 数据域Control
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2020/12/23 14:55
 */

@Slf4j
@RestController
@RequestMapping("/domain/")
@Api(tags = "数据域")
public class DomainControl extends BaseController {

    @Autowired
    DomainService domainService;

    @ApiOperation(value = "查询数据域信息(根据数据域ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据域ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/getDomainById", method = RequestMethod.GET)
    public TeslaBaseResult getDomainById(@RequestParam(name = "id", defaultValue = "0") Integer id) {
        return buildSucceedResult(domainService.getDoaminById(id));
    }

    @ApiOperation(value = "查询数据域信息(根据数据域名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "name", value = "数据域名称", defaultValue = "APP", paramType = "query")
    })
    @RequestMapping(value = "/getDomainByName", method = RequestMethod.GET)
    public TeslaBaseResult getDomainByName(@RequestParam(name = "name", defaultValue = "0") String name) {
        return buildSucceedResult(domainService.getDomainByName(name));
    }

    @ApiOperation(value = "查询数据域信息(根据数据域名称)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "abbreviation", value = "数据域名称简写", defaultValue = "system", paramType = "query")
    })
    @RequestMapping(value = "/getDomainByAbbreviation", method = RequestMethod.GET)
    public TeslaBaseResult getDomainByAbbreviation(@RequestParam(name = "abbreviation", defaultValue = "0") String abbreviation) {
        return buildSucceedResult(domainService.getDomainByAbbreviation(abbreviation));
    }

    @ApiOperation(value = "查询数据域信息(所有)")
    @RequestMapping(value = "/getDomains", method = RequestMethod.GET)
    public TeslaBaseResult getDomains(){
        return buildSucceedResult(domainService.getDomains());
    }


    @ApiOperation(value = "删除数据域(根据数据域ID)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "数据域ID", defaultValue = "0", paramType = "query")
    })
    @RequestMapping(value = "/deleteDomainById", method = RequestMethod.DELETE)
    public TeslaBaseResult deleteDomainById(@RequestParam(name = "id", defaultValue = "0") Integer id) throws Exception {
        return buildSucceedResult(domainService.deleteDomainById(id));
    }

    @ApiOperation(value = "创建数据域")
    @RequestMapping(value = "/createDomain", method = RequestMethod.POST)
    public TeslaBaseResult createDomain(@RequestBody DomainCreateReq req) throws Exception {
        return buildSucceedResult(domainService.createDomain(req));
    }
}
