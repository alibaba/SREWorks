package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.req.trigger.TriggerReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;

/**
 * Trigger API
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Tag(name = "Trigger API")
@RequestMapping("/triggers")
@RestController
public class TriggerController extends AppManagerBaseController {

    @GetMapping("{token}")
    public TeslaBaseResult list(
            @PathVariable("token") @NotEmpty String token,
            @ParameterObject @RequestBody TriggerReq request) {
        request.setToken(token);

        return buildSucceedResult(null);
    }
}
