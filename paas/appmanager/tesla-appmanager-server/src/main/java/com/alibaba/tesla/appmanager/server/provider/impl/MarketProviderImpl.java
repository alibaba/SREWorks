package com.alibaba.tesla.appmanager.server.provider.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.tesla.appmanager.api.provider.AppPackageProvider;
import com.alibaba.tesla.appmanager.api.provider.MarketProvider;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.pagination.Pagination;
import com.alibaba.tesla.appmanager.common.util.*;
import com.alibaba.tesla.appmanager.domain.dto.*;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageVersionCountReq;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;
import com.alibaba.tesla.appmanager.domain.res.apppackage.AppPackageUrlRes;
import com.alibaba.tesla.appmanager.domain.schema.AppPackageSchema;
import com.alibaba.tesla.appmanager.server.assembly.AppPackageDtoConvert;
import com.alibaba.tesla.appmanager.server.repository.AppMetaRepository;
import com.alibaba.tesla.appmanager.server.repository.condition.AppMarketQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.condition.AppMetaQueryCondition;
import com.alibaba.tesla.appmanager.server.repository.domain.AppMetaDO;
import com.alibaba.tesla.appmanager.server.repository.domain.AppPackageDO;
import com.alibaba.tesla.appmanager.server.service.appmarket.AppMarketService;
import com.alibaba.tesla.appmanager.server.service.appoption.AppOptionService;
import com.alibaba.tesla.appmanager.server.service.apppackage.AppPackageService;
import com.alibaba.tesla.appmanager.server.storage.impl.OssStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Autowired
    private AppPackageProvider appPackageProvider;

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

//    @Override
//    public Long lockPublish(){
//
//    }
//
//    @Override
//    public boolean unlockPublish(){
//
//    }
//
    private boolean updateMarketPackageIndex(MarketEndpointDTO marketEndpoint, MarketPackageDTO marketPackage, String relativeRemotePath) throws IOException {

        /**
         * 更新 sw-index.json
         * {
         *    "packages":[{
         *       "appId": "xxx",
         *       "appName": "中文",
         *       "latestTags": ["xxx", "yy"],
         *       "packageVersions": [
         *          "1.1.1+20220608160822137535",
         *       ],
         *       "logoUrl": "applications/upload/logo.png",
         *       "latestComponents":[
         *           {"componentName": "aaa", "componentType": "K8S_MICROSERVICE"}
         *           ...
         *       ],
         *       "category":"交付",
         *       "description": "xxxx",
         *       "urls":{
         *          "1.1.1+20220608160822137535": "applications/upload/1.1.1+20220608160822137535.zip"
         *       }
         *    }]
         * }
         */
        if (StringUtils.equals(marketEndpoint.getEndpointType(), "oss")) {
            JSONObject swIndexObject;
            JSONObject appInfo = null;
            String remoteIndexFilePath = marketEndpoint.getRemotePackagePath() + "/sw-index.json";
            OssStorage client = new OssStorage(
                    marketEndpoint.getEndpoint(), marketEndpoint.getAccessKey(), marketEndpoint.getSecretKey());
            if(client.objectExists(marketEndpoint.getRemoteBucket(), remoteIndexFilePath)){
                File swIndexFile = Files.createTempFile("market", ".json").toFile();
                NetworkUtil.download(
                    client.getObjectUrl(marketEndpoint.getRemoteBucket(), remoteIndexFilePath, 86400),
                    swIndexFile.getAbsolutePath()
                );
                swIndexObject = JSONObject.parseObject(FileUtils.readFileToString(swIndexFile, "UTF-8"));
            }else{
                swIndexObject = new JSONObject();
                swIndexObject.put("packages", new JSONArray());
            }

            JSONArray swIndexPackages = swIndexObject.getJSONArray("packages");

            for(int i=0; i < swIndexPackages.size(); i++){
                if(StringUtils.equals(swIndexPackages.getJSONObject(i).getString("appId"), marketPackage.getAppId())){
                    appInfo = swIndexPackages.getJSONObject(i);
                    break;
                }
            }

            if(appInfo == null){
                appInfo = new JSONObject();
                appInfo.put("appId", marketPackage.getAppId());
                appInfo.put("appName", marketPackage.getAppName());
                if(marketPackage.getAppOptions().getString("category") != null){
                    appInfo.put("category", marketPackage.getAppOptions().getString("category"));
                }
                if(marketPackage.getAppOptions().getString("description") != null){
                    appInfo.put("description", marketPackage.getAppOptions().getString("description"));
                }
                appInfo.put("urls", new JSONObject());
                appInfo.put("packageVersions", new JSONArray());
                swIndexPackages.add(appInfo);
            }

            JSONObject appUrls = appInfo.getJSONObject("urls");
            JSONArray packageVersions = appInfo.getJSONArray("packageVersions");
            appUrls.put(marketPackage.getPackageVersion(), relativeRemotePath);
            packageVersions.add(marketPackage.getPackageVersion());

            File swIndexFile = Files.createTempFile("market_index", ".json").toFile();
            FileUtils.writeStringToFile(swIndexFile, swIndexObject.toString(SerializerFeature.PrettyFormat), true);

            client.putObject(marketEndpoint.getRemoteBucket(), remoteIndexFilePath, swIndexFile.getAbsolutePath());
            client.setObjectAclPublic(marketEndpoint.getRemoteBucket(), remoteIndexFilePath);

        }
        return true;
    }

    @Override
    public String uploadPackage(MarketEndpointDTO marketEndpoint, MarketPackageDTO marketPackage) throws IOException {
        String relativeRemotePath = "applications/" + marketPackage.getAppId() + "/" + marketPackage.getPackageVersion() + ".zip";
        String fullRemotePath = marketEndpoint.getRemotePackagePath() + "/" + relativeRemotePath;
        if (StringUtils.equals(marketEndpoint.getEndpointType(), "oss")) {
            OssStorage client = new OssStorage(
                    marketEndpoint.getEndpoint(), marketEndpoint.getAccessKey(), marketEndpoint.getSecretKey());
            log.info("action=init|message=oss client has initialized|endpoint={}", marketEndpoint.getEndpoint());
            client.putObject(marketEndpoint.getRemoteBucket(), fullRemotePath, marketPackage.getPackageLocalPath());
            client.setObjectAclPublic(marketEndpoint.getRemoteBucket(), fullRemotePath);
            this.updateMarketPackageIndex(marketEndpoint, marketPackage, relativeRemotePath);
            return fullRemotePath;
        }
        return null;
    }

    @Override
    public MarketPackageDTO rebuildAppPackage(AppPackageDTO appPackage, String operator, String appId, String simplePackageVersion) throws IOException {

        String newPackageVersion;
        if(simplePackageVersion == null){
            newPackageVersion = appPackage.getPackageVersion();
        }else{
            newPackageVersion = VersionUtil.buildVersion(simplePackageVersion);
        }

        /**
         * 将包下载到本地并解压
         */
        AppPackageUrlRes appPackageUrl = appPackageProvider.generateUrl(appPackage.getId(), operator);
        File zipFile = Files.createTempFile("market_publish", ".zip").toFile();
        NetworkUtil.download(appPackageUrl.getUrl(), zipFile.getAbsolutePath());
        Path workDirFile = Files.createTempDirectory("market_publish");
        String workDirAbsPath = workDirFile.toFile().getAbsolutePath();
        ZipUtil.unzip(zipFile.getAbsolutePath(), workDirAbsPath);
        FileUtils.deleteQuietly(zipFile);


        /**
         * 将组件包进行解压
         */

        /**
         * 将组件包进行字符串替换
         */

        /**
         * 将组件包进行重新压缩
         */

        /**
         * 针对Application的meta.yaml进行解析替换
         */
        String metaYaml = FileUtils.readFileToString(Paths.get(workDirAbsPath, "/meta.yaml").toFile(), StandardCharsets.UTF_8);
        AppPackageSchema metaYamlObject = SchemaUtil.toSchema(AppPackageSchema.class, metaYaml);
        if(!StringUtils.equals(newPackageVersion, appPackage.getPackageVersion())){
            metaYamlObject.setPackageVersion(newPackageVersion);
        }
        FileUtils.writeStringToFile(Paths.get(workDirAbsPath, "/meta.yaml").toFile(), SchemaUtil.toYamlMapStr(metaYamlObject), StandardCharsets.UTF_8);

        /**
         * 针对应用进行重新压缩
         */
        Path workDirResult = Files.createTempDirectory("market_publish_result");
        String zipPath = workDirResult.resolve("app_package.zip").toString();
        ZipUtil.zipDirectory(workDirResult.resolve("app_package.zip").toString(), workDirFile.toFile());

        MarketPackageDTO marketPackage = new MarketPackageDTO();
        marketPackage.setPackageLocalPath(zipPath);
        marketPackage.setAppId(appId);
        marketPackage.setPackageVersion(newPackageVersion);
        marketPackage.setSimplePackageVersion(simplePackageVersion);
        marketPackage.setAppName(appPackage.getAppName());
        marketPackage.setAppOptions(appPackage.getAppOptions());
        return marketPackage;
    }

}
