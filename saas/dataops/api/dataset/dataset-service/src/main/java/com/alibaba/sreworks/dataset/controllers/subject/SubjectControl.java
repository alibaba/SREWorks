package com.alibaba.sreworks.dataset.controllers.subject;

import com.alibaba.sreworks.dataset.api.subject.SubjectService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.alibaba.tesla.web.controller.BaseController;
import com.alibaba.tesla.common.base.TeslaBaseResult;

/**
 * 数据主题Control
 *
 * @author: fangzong.lyj@alibaba-inc.com
 * @date: 2020/12/23 14:55
 */

@Slf4j
@RestController
@RequestMapping("/subject/")
@Api(tags = "数据主题")
public class SubjectControl extends BaseController {

    @Autowired
    SubjectService subjectService;

    @ApiOperation(value = "查询数据主题(根据数据主题ID)")
    @RequestMapping(value = "/getSubjectById", method = RequestMethod.GET)
    public TeslaBaseResult getSubjectById(@RequestParam(name = "subjectId") Integer subjectId) {
        return buildSucceedResult(subjectService.getSubjectById(subjectId));
    }

    @ApiOperation(value = "查询所有数据主题")
    @RequestMapping(value = "/getSubjects", method = RequestMethod.GET)
    public TeslaBaseResult getSubjects() {
        return buildSucceedResult(subjectService.getSubjects());
    }

}
