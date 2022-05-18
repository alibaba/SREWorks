package com.alibaba.tesla.appmanager.controller;

import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Market Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/markets")
@RestController
public class MarketTestController extends AppManagerBaseController {

    @GetMapping(value = "hello")
    public TeslaBaseResult hello() {
        return buildSucceedResult(null);
    }
}
