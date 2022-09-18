package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.autoconfig.PackageProperties
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.service.GitService
import com.alibaba.tesla.appmanager.common.util.*
import com.alibaba.tesla.appmanager.domain.core.StorageFile
import com.alibaba.tesla.appmanager.domain.req.componentpackage.BuildComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.req.git.GitCloneReq
import com.alibaba.tesla.appmanager.domain.res.componentpackage.LaunchBuildComponentHandlerRes
import com.alibaba.tesla.appmanager.server.factory.JinjaFactory
import com.alibaba.tesla.appmanager.server.service.componentpackage.handler.BuildComponentHandler
import com.alibaba.tesla.appmanager.server.service.imagebuilder.impl.ImageBuilderServiceImpl
import com.alibaba.tesla.appmanager.server.storage.Storage
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 默认构建 Helm Groovy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class HelmComponentBuildHandler implements BuildComponentHandler {

    private static final Logger log = LoggerFactory.getLogger(HelmComponentBuildHandler.class)

    /**
     * 当前脚本类型 (ComponentKindEnum)
     */
    public static final String KIND = DynamicScriptKindEnum.COMPONENT_BUILD.toString()

    /**
     * 当前脚本名称 (指定 SCRIPT_KIND 下唯一)
     */
    public static final String NAME = "HelmDefault"

    /**
     * 当前内置 Handler 版本
     */
    public static final Integer REVISION = 40

    private static final String KEY_HELM_CHART = "helm_chart"

    @Autowired
    private PackageProperties packageProperties

    @Autowired
    private Storage storage

    @Autowired
    private ImageBuilderServiceImpl imageBuilderService

    @Autowired
    private GitService gitService

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
        // 扫描 package options，构建所有需要的镜像，并存储到 packageDir 中
        def logContent = new StringBuilder()
        options.put("appId", appId)
        options.put("componentType", componentType)
        options.put("componentName", componentName)
        options.put("version", version)
        log.info("all images have built|appId={}|componentType={}|componentName={}|packageVersion={}|",
                appId, componentType, componentName, version)

        // Clone 仓库
        def gitRepo = options.getString("repo")
        def chartUrl = options.getString("chartUrl")
        def chartDownload = options.getBooleanValue("chartDownload")
        def repoUrl = options.getString("repoUrl")
        def chartName = options.getString("chartName")
        def chartVersion = options.getString("chartVersion")
        if (StringUtils.isNotEmpty(gitRepo)) {
            gitService.cloneRepo(logContent, GitCloneReq.builder()
                    .repo(gitRepo)
                    .branch(options.getString("branch"))
                    .commit(options.getString("commit"))
                    .repoPath(options.getString("repoPath"))
                    .ciAccount(options.getString("ciAccount"))
                    .ciToken(options.getString("ciToken"))
                    .build(), packageDir)
            logContent.append(String.format("git repo has cloned|repo=%s\n", gitRepo))
        } else if (StringUtils.isNotEmpty(chartUrl)) {
            def zipFile = Files.createTempFile(KEY_HELM_CHART, ".zip").toFile()
            def zipFileAbsPath = zipFile.getAbsolutePath()
            NetworkUtil.download(chartUrl, zipFileAbsPath)
            ZipUtil.unzip(zipFileAbsPath, packageDir.toFile().getAbsolutePath())
            FileUtils.deleteQuietly(zipFile)
            logContent.append(String.format("helm chart has downloaded|url=%s\n", chartUrl))
        } else if (StringUtils.isNotEmpty(repoUrl)) {
            if (chartDownload) {
                String cmd = String.format("helm pull --repo %s %s --version %s -d %s", repoUrl, chartName, chartVersion, packageDir.toFile().getAbsolutePath())
                String ret = CommandUtil.runLocalCommand(cmd)
                logContent.append(String.format("helm pull|cmd=%s|ret=%s\n", cmd, ret))
            }
        } else {
            def errorMessage = String.format("no gitRepo/chartUrl found|options=%s\n", JSONObject.toJSONString(options))
            logContent.append(errorMessage)
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, errorMessage)
        }

        // 创建 meta.yaml 元信息存储到 packageDir 顶层目录中
        def jinjava = JinjaFactory.getJinjava()
        def template = getTemplate("default_helm.tpl")
        def metaYamlContent = jinjava.render(template, options)
        def metaYamlFile = Paths.get(packageDir.toString(), "meta.yaml").toFile()
        FileUtils.writeStringToFile(metaYamlFile, metaYamlContent, StandardCharsets.UTF_8)
        log.info("meta yaml config has rendered|appId={}|componentType={}|componentName={}|packageVersion={}",
                appId, componentType, componentName, version)

        // 将 packageDir 打包为 zip 文件
        String zipPath = packageDir.resolve("app_package.zip").toString()
        def zp = new ZipParameters()
        zp.setIncludeRootFolder(false)
        new ZipFile(zipPath).addFolder(new File(packageDir.toString()), zp)
        def targetFileMd5 = StringUtil.getMd5Checksum(zipPath)
        log.info("zip file has generated|appId={}|componentType={}|componentName={}|packageVersion={}|" +
                "zipPath={}|md5={}", appId, componentType, componentName, version,
                zipPath, targetFileMd5)

        // 上传导出包到 Storage 中
        String bucketName = packageProperties.getBucketName()
        String remotePath = PackageUtil
                .buildComponentPackageRemotePath(appId, componentType, componentName, version)
        storage.putObject(bucketName, remotePath, zipPath)
        log.info("component package has uploaded to storage|bucketName={}|" +
                "remotePath={}|localPath={}", bucketName, remotePath, zipPath)

        // 删除临时数据 (正常流程下)
        try {
            FileUtils.deleteDirectory(packageDir.toFile())
        } catch (Exception ignored) {
            log.warn("cannot delete component package build directory|directory={}", packageDir.toString())
        }
        LaunchBuildComponentHandlerRes res = LaunchBuildComponentHandlerRes.builder()
                .logContent(logContent.toString())
                .storageFile(new StorageFile(bucketName, remotePath))
                .packageMetaYaml(metaYamlContent)
                .packageMd5(targetFileMd5)
                .build()
        return res
    }

    private static String getTemplate(String templateName) {
        def config = new ClassPathResource("jinja/" + templateName)
        return IOUtils.toString(config.getInputStream(), StandardCharsets.UTF_8)
    }
}
