package com.alibaba.tesla.appmanager.api.provider;

import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketAppItemDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketEndpointDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketPackageDTO;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;

import java.io.IOException;

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

    /**
     * 重新构建应用包，可以改变appId和packageVersion
     */
    MarketPackageDTO rebuildAppPackage(AppPackageDTO appPackage, String appId, String simplePackageVersion, String operator) throws IOException;

//    /**
//     * 更新市场包索引
//     */
//    boolean updateMarketPackageIndex(MarketEndpointDTO marketEndpoint, MarketPackageDTO marketPackage) throws IOException;

    /**
     * 上传市场包
     */
    String uploadPackage(MarketEndpointDTO marketEndpoint, MarketPackageDTO marketPackage) throws IOException;
}


