package com.alibaba.tesla.appmanager.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.AppPackageProvider;
import com.alibaba.tesla.appmanager.api.provider.MarketProvider;
import com.alibaba.tesla.appmanager.auth.controller.AppManagerBaseController;
import com.alibaba.tesla.appmanager.common.util.*;
import com.alibaba.tesla.appmanager.domain.dto.AppPackageDTO;
import com.alibaba.tesla.appmanager.domain.req.apppackage.AppPackageQueryReq;
import com.alibaba.tesla.appmanager.domain.req.market.MarketAppListReq;
import com.alibaba.tesla.appmanager.domain.req.market.MarketCheckReq;
import com.alibaba.tesla.appmanager.domain.req.market.MarketPublishReq;
import com.alibaba.tesla.appmanager.domain.res.apppackage.AppPackageUrlRes;
import com.alibaba.tesla.appmanager.domain.schema.AppPackageSchema;
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
import java.nio.charset.StandardCharsets;
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
    public TeslaBaseResult endpointCheck(MarketCheckReq request) throws IOException {
        JSONObject result = new JSONObject();
        result.put("write", false);
        result.put("read", false);
        result.put("hasInit", false);
        if(StringUtils.isNotBlank(request.getAccessKey())){
            /**
             *  测试写入
             */
            OssStorage client = new OssStorage(request.getEndpoint(), request.getAccessKey(), request.getSecretKey());
            Path tempFile = Files.createTempFile("sw", ".txt");
            client.putObject(request.getRemoteBucket(), request.getRemotePackagePath(), tempFile.toAbsolutePath().toString());

            String remoteFilePath = request.getRemotePackagePath() + "/" + tempFile.getFileName().toString();
            result.put("write", client.objectExists(request.getRemoteBucket(), remoteFilePath));
        }
        /**
         *  测试读取
         */
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

        String remotePackageVersion = VersionUtil.buildVersion(request.getRemoteSimplePackageVersion());
        String remoteAppId;
        if (StringUtils.isNotBlank(request.getRemoteAppId())){
            remoteAppId = request.getRemoteAppId();
        }else{
            remoteAppId = appPackageInfo.getAppId();
        }
        String fullRemotePath = request.getRemotePackagePath() + "/applications/" + remoteAppId + "/" + remotePackageVersion + ".zip";
        JSONObject result = new JSONObject();
        result.put("filePath", fullRemotePath);
        result.put("remoteAppId", remoteAppId);
        result.put("remotePackageVersion", remotePackageVersion);
        result.put("remoteBucket", request.getRemoteBucket());

        /**
         * 将包下载到本地并解压
         */
        AppPackageUrlRes appPackageUrl = appPackageProvider.generateUrl(appPackageInfo.getId(), getOperator(auth));
        File zipFile = Files.createTempFile("market_publish", ".zip").toFile();
        String zipFileAbsPath = zipFile.getAbsolutePath();
        NetworkUtil.download(appPackageUrl.getUrl(), zipFile.getAbsolutePath());
        Path workDirFile = Files.createTempDirectory("market_publish");
        String workDirAbsPath = workDirFile.toFile().getAbsolutePath();
        ZipUtil.unzip(zipFileAbsPath, workDirAbsPath);
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
        metaYamlObject.setPackageVersion(remotePackageVersion);
        FileUtils.writeStringToFile(Paths.get(workDirAbsPath, "/meta.yaml").toFile(), SchemaUtil.toYamlMapStr(metaYamlObject), StandardCharsets.UTF_8);

        /**
         * 针对应用进行重新压缩
         */
        Path workDirResult = Files.createTempDirectory("market_publish_result");
        String zipPath = workDirResult.resolve("app_package.zip").toString();
        ZipUtil.zipDirectory(workDirResult.resolve("app_package.zip").toString(), workDirFile.toFile());

        /**
         * 将包上传到远端市场
         */
        if (StringUtils.equals(request.getEndpointType(), "oss")){
            OssStorage client = new OssStorage(
                    request.getEndpoint(), request.getAccessKey(), request.getSecretKey());
            log.info("action=init|message=oss client has initialized|endpoint={}", request.getEndpoint());
            client.putObject(request.getRemoteBucket(), fullRemotePath, zipPath);
        }else{
            return buildClientErrorResult("no support endpoint type");
        }

        return buildSucceedResult(result);

    }

    @GetMapping(value = "download")
    @ResponseBody
    public TeslaBaseResult download(OAuth2Authentication auth) {
        return buildSucceedResult(null);
    }
}
