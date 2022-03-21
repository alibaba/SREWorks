package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.MarketProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.ClassUtil;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageDTO;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageVersionCountDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketAppItemDTO;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageVersionCountReq;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;
import com.alibaba.tesla.appmanager.server.assembly.AppPackageDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.AppMetaRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppMarketQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppMetaQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppMetaDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageDO;
import com.alibaba.tesla.appmanager.server.service.appmarket.AppMarketService;
import com.alibaba.tesla.appmanager.server.service.appoption.AppOptionService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author qianmo.zm@alibaba-inc.com
 * @date 2020/11/19.
 */
@Slf4j
@Service
public class MarketProviderImpl implements MarketProvider {

    @Autowired
    private AppPackageDtoConvert appPackageDtoConvert;

    @Autowired
    private AppMarketService appMarketService;

    @Autowired
    private AppMetaRepository appMetaRepository;

    @Autowired
    private AppOptionService appOptionService;

    @Autowired
    private AppPackageService appPackageService;

    @Override
    public Pagination<MarketAppItemDTO> list(MarketAppListReq request) {
        AppMarketQueryCondition condition = AppMarketQueryCondition.builder()
                .tag(DefaultConstant.ON_SALE)
                .optionKey(request.getOptionKey())
                .optionValue(request.getOptionValue())
                .page(request.getPage())
                .pageSize(request.getPageSize())
                .withBlobs(request.isWithBlobs())
                .build();
        Pagination<AppPackageDO> packages = appMarketService.list(condition);

        List<AppMetaDO> metaList = new ArrayList<>();
        List<AppPackageVersionCountDTO> countVersionList = new ArrayList<>();
        if (!packages.isEmpty()) {
            List<String> appIds = packages.getItems().stream()
                    .map(AppPackageDO::getAppId)
                    .collect(Collectors.toList());
            metaList.addAll(appMetaRepository.selectByCondition(
                    AppMetaQueryCondition.builder()
                            .appIdList(appIds)
                            .withBlobs(true)
                            .build()));
            countVersionList.addAll(appPackageService.countVersion(
                    AppPackageVersionCountReq.builder()
                            .appIds(appIds)
                            .tag(DefaultConstant.ON_SALE)
                            .build()));
        }
        return Pagination.transform(packages, item -> transform(item, metaList, countVersionList));
    }

    /**
     * 转换 appPackage，增加附加数据，并获取实际市场需要的 Item 对象
     *
     * @param item             appPackage item
     * @param metaList         元信息
     * @param countVersionList 版本计数列表
     * @return
     */
    private MarketAppItemDTO transform(
            AppPackageDO item, List<AppMetaDO> metaList, List<AppPackageVersionCountDTO> countVersionList) {
        AppPackageDTO mid = appPackageDtoConvert.to(item);
        if (Objects.nonNull(mid)) {
            AppMetaDO appMetaDO = metaList.stream()
                    .filter(m -> StringUtils.equals(m.getAppId(), mid.getAppId()))
                    .findFirst()
                    .orElse(null);
            if (Objects.nonNull(appMetaDO)) {
                JSONObject optionMap = appOptionService.getOptionMap(appMetaDO.getAppId());
                String name = appMetaDO.getAppId();
                if (StringUtils.isNotEmpty(optionMap.getString("name"))) {
                    name = optionMap.getString("name");
                }
                mid.setAppName(name);
                mid.setAppOptions(optionMap);
            }
        }
        MarketAppItemDTO result = new MarketAppItemDTO();
        ClassUtil.copy(mid, result);

        // 填充 package version 计数
        List<AppPackageVersionCountDTO> filteredCountVersion = countVersionList.stream()
                .filter(countVersion -> item.getAppId().equals(countVersion.getAppId()))
                .collect(Collectors.toList());
        if (filteredCountVersion.size() > 0) {
            result.setPackageCount(filteredCountVersion.get(0).getPackageCount());
        } else {
            result.setPackageCount(0L);
        }
        return result;
    }
}
