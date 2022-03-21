package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketAppItemDTO;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;

/**
 * 应用市场接口
 *
 * @author qianmo.zm@alibaba-inc.com
 */
public interface MarketProvider {

    /**
     * 获取本地市场内容
     */
    Pagination<MarketAppItemDTO> list(MarketAppListReq request);
}
