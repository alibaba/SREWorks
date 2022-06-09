package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppPackageProvider;
import com.alibaba.tesla.appmanager.api.provider.MarketProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.util.NetworkUtil;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketEndpointDTO;
import com.alibaba.tesla.appmanager.domain.dto.MarketPackageDTO;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageQueryReq;
import com.alibaba.tesla.appmanager.domain.req.market.*;
import com.alibaba.tesla.appmanager.server.storage.impl.OssStorage;
import com.alibaba.tesla.common.base.TeslaBaseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Autowired
    private AppPackageProvider appPackageProvider;


    @GetMapping(value = "/apps")
    public TeslaBaseResult listApp(MarketAppListReq request) {
        return buildSucceedResult(marketProvider.list(request));
    }

    @PostMapping(value = "/check")
    public TeslaBaseResult endpointCheck(@RequestBody MarketCheckReq request, OAuth2Authentication auth) throws IOException {
        JSONObject result = new JSONObject();
        result.put("write", false);
        result.put("read", false);
        result.put("hasInit", false);


        String remotePackagePath = request.getRemotePackagePath();
        if(remotePackagePath.startsWith("/")){
            /**
             * 用户传入的路径如果以/开头，则自动适配
             */
            remotePackagePath = remotePackagePath.substring(1);
        }

        if(StringUtils.equals(request.getEndpointType(), "oss")){
            OssStorage client = new OssStorage(request.getEndpoint(), request.getAccessKey(), request.getSecretKey());
            if(StringUtils.isNotBlank(request.getAccessKey())){
                /**
                 *  测试写入
                 */
                Path tempFile = Files.createTempFile("sw", ".txt");
                String remoteFilePath = remotePackagePath + "/" + tempFile.getFileName().toString();
                client.putObject(request.getRemoteBucket(), remoteFilePath, tempFile.toAbsolutePath().toString());
                boolean writeTest = client.objectExists(request.getRemoteBucket(), remoteFilePath);
                result.put("write", writeTest);
                client.removeObject(request.getRemoteBucket(), remoteFilePath);
                log.info("action=check|message=check oss write|endpoint={}|bucket={}|writePath={}",
                        request.getEndpoint(), request.getRemoteBucket(), request.getRemotePackagePath());
            }
            /**
             *  测试读取
             */
            try{
                client.listObjects(request.getRemoteBucket(), remotePackagePath);
                result.put("read", true);
            }catch (Exception e) {
                log.info("action=check|message=check oss read fail|{}", e);
            }
        }else{
            buildClientErrorResult("not support endpointType " + request.getEndpointType());
        }


        return buildSucceedResult(result);
    }


    @PostMapping(value = "publish")
    @ResponseBody
    public TeslaBaseResult publish(@RequestBody MarketPublishReq request, OAuth2Authentication auth) throws IOException {

        /**
         * 根据appPackageId找到包
         */
        AppPackageDTO appPackageInfo = appPackageProvider.get(AppPackageQueryReq.builder().id(request.getAppPackageId())
                .withBlobs(true)
                .build(), getOperator(auth));

        String remoteAppId;
        if (StringUtils.isNotBlank(request.getRemoteAppId())){
            remoteAppId = request.getRemoteAppId();
        }else{
            remoteAppId = appPackageInfo.getAppId();
        }
        String remotePackagePath = request.getRemotePackagePath();
        if(remotePackagePath.startsWith("/")){
            /**
             * 用户传入的路径如果以/开头，则自动适配
             */
            remotePackagePath = remotePackagePath.substring(1);
        }

        MarketPackageDTO marketPackage = marketProvider.rebuildAppPackage(
                appPackageInfo, getOperator(auth), remoteAppId, request.getRemoteSimplePackageVersion());

        JSONObject result = new JSONObject();
        result.put("remoteAppId", remoteAppId);
        result.put("remotePackageVersion", marketPackage.getPackageVersion());
        result.put("remoteBucket", request.getRemoteBucket());

        MarketEndpointDTO marketEndpoint = new MarketEndpointDTO();
        marketEndpoint.setEndpoint(request.getEndpoint());
        marketEndpoint.setEndpointType(request.getEndpointType());
        marketEndpoint.setAccessKey(request.getAccessKey());
        marketEndpoint.setSecretKey(request.getSecretKey());
        marketEndpoint.setRemoteBucket(request.getRemoteBucket());
        marketEndpoint.setRemotePackagePath(remotePackagePath);

        /**
         * 将包上传到远端市场
         */

        String filePath = marketProvider.uploadPackage(marketEndpoint, marketPackage);
        result.put("filePath", filePath);
        if(filePath == null){
            return buildClientErrorResult("upload package failed");
        }

        return buildSucceedResult(result);

    }

    @GetMapping(value = "detail")
    @ResponseBody
    public TeslaBaseResult detail(MarketAppDetailReq request, OAuth2Authentication auth) throws IOException {
        JSONObject detailObject = new JSONObject();
        JSONObject appInfo = null;
        if(StringUtils.startsWith(request.getRemoteUrl(), "oss://")){
            String swIndexUrl = "https://" + Paths.get(request.getRemoteUrl().replace("oss://", ""), "sw-index.json").toString();
            File swIndexFile = Files.createTempFile("market", ".json").toFile();
            NetworkUtil.download(swIndexUrl, swIndexFile.getAbsolutePath());
            JSONObject swIndexObject = JSONObject.parseObject(FileUtils.readFileToString(swIndexFile, "UTF-8"));
            JSONArray swIndexPackages = swIndexObject.getJSONArray("packages");
            for(int i=0; i < swIndexPackages.size(); i++){
                if(StringUtils.equals(swIndexPackages.getJSONObject(i).getString("appId"), request.getAppId())){
                    appInfo = swIndexPackages.getJSONObject(i);
                    break;
                }
            }
            return buildSucceedResult(appInfo);
        }
        return buildSucceedResult(detailObject);
    }

    @GetMapping(value = "packageList")
    @ResponseBody
    public TeslaBaseResult packageList(MarketAppPackageListReq request, OAuth2Authentication auth) throws IOException {
        JSONArray packageArray = new JSONArray();
        String[] remoteUrls = request.getRemoteUrls().split(",");

        for(String remoteUrl: remoteUrls){
            String swIndexUrl = "https://" + Paths.get(remoteUrl.replace("oss://", ""), "sw-index.json").toString();
            File swIndexFile = Files.createTempFile("market", ".json").toFile();
            NetworkUtil.download(swIndexUrl, swIndexFile.getAbsolutePath());
            JSONObject swIndexObject = JSONObject.parseObject(FileUtils.readFileToString(swIndexFile, "UTF-8"));
            JSONArray swIndexPackages = swIndexObject.getJSONArray("packages");
            packageArray.addAll(swIndexPackages);
        }

        JSONObject result = new JSONObject();
        result.put("items", packageArray);
        return buildSucceedResult(result);
    }

    @GetMapping(value = "download")
    @ResponseBody
    public TeslaBaseResult download(OAuth2Authentication auth) {
        return buildSucceedResult(null);
    }
}
