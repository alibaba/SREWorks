package com.alibaba.sreworks.avatarmanage.server.controllers;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.sreworks.domain.DO.Avatar;
import com.alibaba.sreworks.domain.repository.AvatarRepository;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import com.alibaba.tesla.web.controller.BaseController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author jinghua.yjh
 */
@Slf4j
@RestController
@RequestMapping("/avatarmanage/avatar")
@Api(tags = "头像")
public class AvatarController extends BaseController {

    @Autowired
    AvatarRepository avatarRepository;

    @ApiOperation(value = "上传头像")
    @RequestMapping(value = "upload", method = RequestMethod.POST)
    public TeslaBaseResult upload(@RequestParam("file") MultipartFile file) throws IOException {
        Avatar avatar = avatarRepository.saveAndFlush(new Avatar(new String(file.getBytes())));
        return buildSucceedResult(avatar.getId());
    }

    @ApiOperation(value = "查看头像")
    @RequestMapping(value = "get", method = RequestMethod.GET)
    public void show(Long id, HttpServletResponse response) throws IOException {
        Avatar avatar = avatarRepository.findFirstById(id);
        String content = avatar == null ? "" : avatar.getContent();
        response.setContentType("image/jpeg");
        OutputStream out = response.getOutputStream();
        out.write(content.getBytes());
        out.flush();
        out.close();
    }

}
