package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.PackageUtil
import com.alibaba.tesla.appmanager.common.util.StringUtil
import com.alibaba.tesla.appmanager.common.util.ZipUtil
import com.alibaba.tesla.appmanager.domain.core.StorageFile
import com.alibaba.tesla.appmanager.domain.req.componentpackage.BuildComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.res.componentpackage.LaunchBuildComponentHandlerRes
import com.alibaba.tesla.appmanager.server.factory.JinjaFactory
import com.alibaba.tesla.appmanager.server.service.componentpackage.handler.BuildComponentHandler
import com.alibaba.tesla.appmanager.server.storage.Storage
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 默认 Script 组件 Groovy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class ScriptComponentBuildHandler implements BuildComponentHandler {

    private static final Logger log = LoggerFactory.getLogger(ScriptComponentBuildHandler.class)

    /**
     * 当前内置 Handler 类型
     */
    public static final String KIND = DynamicScriptKindEnum.BUILD_SCRIPT_COMPONENT.toString()

    /**
     * 当前内置 Handler 名称
     */
    public static final String NAME = DefaultConstant.DEFAULT_GROOVY_HANDLER

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 0

    private static final String TEMPLATE_SCRIPT_FILENAME = "default_script_component.tpl"

    @Autowired
    private PackageProperties packageProperties

    @Autowired
    private Storage storage

    /**
     * 构建一个实体 Component Package
     *
     * @param request ComponentPackage 创建任务对象
     * @return 实体包信息
     */
    @Override
    LaunchBuildComponentHandlerRes launch(BuildComponentHandlerReq request) {
        def appId = request.getAppId()
        def componentType = request.getComponentType()
        def componentName = request.getComponentName()
        def version = request.getVersion()
        def options = request.getOptions()

        // 创建当前组件包的临时组装目录，用于存储 meta 信息及构建后的镜像
        def packageDir
        try {
            packageDir = Files.createTempDirectory("appmanager_component_package_")
            packageDir.toFile().deleteOnExit()
        } catch (IOException e) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "cannot create temp directory", e)
        }

        // 创建 meta.yaml 元信息存储到 packageDir 顶层目录中
        def jinjava = JinjaFactory.getJinjava()
        def template = getTemplate(TEMPLATE_SCRIPT_FILENAME)
        def optionsClone = (JSONObject) options.clone()
        options.put("appId", appId)
        options.put("componentType", componentType)
        options.put("componentName", componentName)
        options.put("version", version)
        options.put("options", optionsClone)
        def metaYamlContent = jinjava.render(template, options)
        def metaYamlFile = Paths.get(packageDir.toString(), "meta.yaml").toFile()
        FileUtils.writeStringToFile(metaYamlFile, metaYamlContent, StandardCharsets.UTF_8)
        log.info("meta yaml config has rendered||appId={}||componentType={}||componentName={}||packageVersion={}",
                appId, componentType, componentName, version)

        // 将 packageDir 打包为 zip 文件
        String zipPath = packageDir.resolve("app_package.zip").toString()
        Files.list(packageDir).map({ p -> p.toFile() }).forEach({ item ->
            if (item.isDirectory()) {
                ZipUtil.zipDirectory(zipPath, item)
            } else {
                ZipUtil.zipFile(zipPath, item)
            }
        })
        def targetFileMd5 = StringUtil.getMd5Checksum(zipPath)
        log.info("zip file has generated||appId={}||componentType={}||componentName={}||packageVersion={}||" +
                "zipPath={}||md5={}", appId, componentType, componentName, version,
                zipPath, targetFileMd5)

        // 上传导出包到 Storage 中
        String bucketName = packageProperties.getBucketName()
        String remotePath = PackageUtil
                .buildComponentPackageRemotePath(appId, componentType, componentName, version)
        storage.putObject(bucketName, remotePath, zipPath)
        log.info("component package has uploaded to storage||bucketName={}||" +
                "remotePath={}||localPath={}", bucketName, remotePath, zipPath)

        // 删除临时数据 (正常流程下)
        try {
            FileUtils.deleteDirectory(packageDir.toFile())
        } catch (Exception ignored) {
            log.warn("cannot delete component package build directory||directory={}", packageDir.toString())
        }
        LaunchBuildComponentHandlerRes res = LaunchBuildComponentHandlerRes.builder()
                .logContent("get zip file from abm chart succeed")
                .storageFile(new StorageFile(bucketName, remotePath))
                .packageMetaYaml(metaYamlContent)
                .packageMd5(targetFileMd5)
                .build()
        return res
    }

    /**
     * 获取指定名称的 resources 下的 Jinja 模板
     * @param templateName 模板名称
     * @return 模板内容
     */
    private static String getTemplate(String templateName) {
        def config = new ClassPathResource("jinja/" + templateName)
        return IOUtils.toString(config.getInputStream(), StandardCharsets.UTF_8)
    }
}
