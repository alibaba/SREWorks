package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.tesla.appmanager.api.provider.MarketProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 应用市场 Controller
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RequestMapping("/market")
@RestController
public class MarketController extends AppManagerBaseController {

    @Autowired
    private MarketProvider marketProvider;

    @GetMapping(value = "/apps")
    public TeslaBaseResult listApp(MarketAppListReq request) {
        return buildSucceedResult(marketProvider.list(request));
    }
}
