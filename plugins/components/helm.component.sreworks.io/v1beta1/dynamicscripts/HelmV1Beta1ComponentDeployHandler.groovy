package dynamicscripts

import com.alibaba.fastjson.JSONObject
import com.alibaba.tesla.appmanager.autoconfig.SystemProperties
import com.alibaba.tesla.appmanager.common.enums.ComponentInstanceStatusEnum
import com.alibaba.tesla.appmanager.common.enums.DeployComponentStateEnum
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode
import com.alibaba.tesla.appmanager.common.exception.AppException
import com.alibaba.tesla.appmanager.common.util.CommandUtil
import com.alibaba.tesla.appmanager.common.util.NetworkUtil
import com.alibaba.tesla.appmanager.common.util.ZipUtil
import com.alibaba.tesla.appmanager.domain.req.componentinstance.ReportRtComponentInstanceStatusReq
import com.alibaba.tesla.appmanager.domain.req.deploy.GetDeployComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.req.deploy.LaunchDeployComponentHandlerReq
import com.alibaba.tesla.appmanager.domain.res.deploy.GetDeployComponentHandlerRes
import com.alibaba.tesla.appmanager.domain.res.deploy.LaunchDeployComponentHandlerRes
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory
import com.alibaba.tesla.appmanager.server.provider.impl.ClusterProviderImpl
import com.alibaba.tesla.appmanager.server.service.deploy.handler.DeployComponentHandler
import com.alibaba.tesla.appmanager.server.service.rtcomponentinstance.RtComponentInstanceService
import io.fabric8.kubernetes.api.model.NamespaceBuilder
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder
import io.fabric8.kubernetes.client.KubernetesClientException
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

/**
 * 默认构建 Helm Groovy Handler
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
class HelmV1Beta1ComponentDeployHandler implements DeployComponentHandler {

    private static final Logger log = LoggerFactory.getLogger(HelmV1Beta1ComponentDeployHandler.class)

    private static final String KEY_COMPONENT_PACKAGE_URL = "appmanager_deploy_component_package"

    /**
     * 当前脚本类型 (ComponentKindEnum)
     */
    public static final String KIND = DynamicScriptKindEnum.COMPONENT_DEPLOY.toString()

    /**
     * 当前脚本名称 (指定 SCRIPT_KIND 下唯一)
     */
    public static final String NAME = "HelmV1Beta1Default"

    /**
     * 当前内置 Handler 版
     */
    public static final Integer REVISION = 4

    private static final String ANNOTATIONS_VERSION = "annotations.appmanager.oam.dev/version"
    private static final String ANNOTATIONS_COMPONENT_INSTANCE_ID = "annotations.appmanager.oam.dev/componentInstanceId"
    private static final String ANNOTATIONS_APP_INSTANCE_NAME = "annotations.appmanager.oam.dev/appInstanceName"

    @Autowired
    private KubernetesClientFactory kubernetesClientFactory

    @Autowired
    private SystemProperties systemProperties

    @Autowired
    private ClusterProviderImpl clusterProvider

    @Autowired
    private RtComponentInstanceService componentInstanceService

    @Override
    LaunchDeployComponentHandlerRes launch(LaunchDeployComponentHandlerReq request) {
        def packageDir = getPackageDir(request)
        def componentSchema = request.getComponentSchema()
        def appId = request.getAppId()
        def componentName = request.getComponentName()
        def stageId = request.getStageId()
        def spec = (JSONObject) componentSchema.getSpec().getWorkload().getSpec()

        // 获取 helm install 的 name
        def name = getMetaName(appId, componentName, stageId)
        if (!StringUtils.isEmpty(spec.getString("name"))) {
            name = spec.getString("name")
        }

        // 如果存在 namespace autoCreate 参数，那么在目标集群中动态创建该 namespace
        def options = request.getComponentOptions()
        if (CollectionUtils.isNotEmpty(options.getScopes())) {
            List<DeployAppSchema.SpecComponentScope> scopeList = options.getScopes()
            for (DeployAppSchema.SpecComponentScope scope : scopeList) {
                if ("namespace".equalsIgnoreCase(scope.getScopeRef().getKind())) {
                    def namespace = scope.getScopeRef().getName()
                    if (scope.getScopeRef().getSpec() != null) {
                        def autoCreate = scope.getScopeRef().getSpec().getBooleanValue("autoCreate")
                        if (autoCreate) {
                            def cluster = request.getClusterId()
                            def client = kubernetesClientFactory.get(cluster)
                            def namespaceObj = client.namespaces().withName(namespace).get()
                            if (namespaceObj == null) {
                                log.info("find autocreate flag in request, create namespace {} in cluster {}",
                                        namespace, cluster)
                                def ns = new NamespaceBuilder()
                                        .withMetadata(new ObjectMetaBuilder().withName(namespace).build())
                                        .build()
                                try {
                                    client.namespaces().create(ns)
                                } catch (KubernetesClientException e) {
                                    if (e.getCode() == 409) {
                                        log.error("namespace {} already exists, skip", namespace);
                                    } else {
                                        throw e;
                                    }
                                }
                                log.info("namespace {} in cluster {} has created", namespace, cluster)
                            } else {
                                log.info("namespace {} in cluster {} already exists", namespace, cluster)
                            }
                        }
                    }
                }
            }
        }

        // 执行安装
        apply(request, name, packageDir)

        // 返回 name 到外界
        spec.put("name", name)
        log.info("name {} has put into component schema", name)

        // 上报状态
        def annotations = (JSONObject) componentSchema.getSpec().getWorkload().getMetadata().getAnnotations()
        def version = (String) annotations.getOrDefault(ANNOTATIONS_VERSION, "")
        def componentInstanceId = (String) annotations.getOrDefault(ANNOTATIONS_COMPONENT_INSTANCE_ID, "")
        def appInstanceName = (String) annotations.getOrDefault(ANNOTATIONS_APP_INSTANCE_NAME, "")
        componentInstanceService.report(ReportRtComponentInstanceStatusReq.builder()
                .componentInstanceId(componentInstanceId)
                .appInstanceName(appInstanceName)
                .clusterId(request.getClusterId())
                .namespaceId(request.getNamespaceId())
                .stageId(request.getStageId())
                .appId(request.getAppId())
                .componentType(request.getComponentType())
                .componentName(request.getComponentName())
                .version(version)
                .status(ComponentInstanceStatusEnum.COMPLETED.toString())
                .conditions(new ArrayList<>())
                .build())

        try {
            FileUtils.deleteDirectory(Paths.get(packageDir).toFile())
        } catch (Exception ignored) {
            log.warn("cannot delete component package directory|directory={}", packageDir)
        }
        LaunchDeployComponentHandlerRes res = LaunchDeployComponentHandlerRes.builder()
                .componentSchema(componentSchema)
                .build()
        return res
    }

    @Override
    GetDeployComponentHandlerRes get(GetDeployComponentHandlerReq request) {
        def cluster = request.getClusterId()
        def namespace = request.getNamespaceId()
        def name = getMetaName(request.getAppId(), request.getComponentName(), request.getStageId())

        def clusterConfig = clusterProvider.get(cluster).getClusterConfig()
        def token = clusterConfig.getString("oauthToken")
        def apiserver = clusterConfig.getString("masterUrl")
        def kube = clusterConfig.getString("kube")

        String[] command
        def kubeFile = null
        if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(apiserver)) {
            command = new String[]{
                "/app/helm", "status", name,
                String.format("--kube-token=%s", token),
                String.format("--kube-apiserver=%s", apiserver),
                "--kube-ca-file=/run/secrets/kubernetes.io/serviceaccount/ca.crt",
                "-n", namespace, "-o", "json"
            }
//            command = String.format(
//                    "/app/helm status %s --kube-token=%s --kube-apiserver=%s " +
//                            "--kube-ca-file=/run/secrets/kubernetes.io/serviceaccount/ca.crt -n %s -o json",
//                    name, token, apiserver, namespace
//            )
        } else if (StringUtils.isNotEmpty(kube)) {
            kubeFile = Files.createTempFile("kubeconfig", ".json").toFile();
            FileUtils.writeStringToFile(kubeFile, kube, StandardCharsets.UTF_8)
            command = new String[]{
                "/app/helm", "status", name,
                "--kubeconfig", kubeFile.getAbsolutePath(),
                "-n", namespace, "-o", "json"
            }
//            command = String.format(
//                    "/app/helm status %s --kubeconfig %s -n %s -o json", name, kubeFile.getAbsolutePath(), namespace)
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find cluster authorization info|clusterId=%s", cluster))
        }
        def stdout = CommandUtil.runLocalCommand(command)
        if (kubeFile != null) {
            def path = kubeFile.getAbsoluteFile()
            if (!kubeFile.delete()) {
                log.error("cannot delete temp kubeconfig file, please check|path={}", path)
            }
        }

        def status = JSONObject.parseObject(stdout).getJSONObject("info").getString("status")
        DeployComponentStateEnum finalStatus
        switch (status) {
            case "deployed":
                finalStatus = DeployComponentStateEnum.SUCCESS
                break
            default:
                finalStatus = DeployComponentStateEnum.FAILURE
                break
        }
        GetDeployComponentHandlerRes res = GetDeployComponentHandlerRes.builder()
                .status(finalStatus)
                .message(stdout)
                .build()
        return res
    }

    private static String getPackageDir(LaunchDeployComponentHandlerReq request) {
        def remoteUrl = request.getComponentPackageUrl()
        if (StringUtils.isEmpty(remoteUrl)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "empty \"componentPackageUrl\" parameter")
        }
        def zipFile = Files.createTempFile(KEY_COMPONENT_PACKAGE_URL, ".zip").toFile()
        def zipFileAbsPath = zipFile.getAbsolutePath()
        NetworkUtil.download(remoteUrl, zipFile.getAbsolutePath())
        def workDirFile = Files.createTempDirectory(KEY_COMPONENT_PACKAGE_URL)
        def workDirAbsPath = workDirFile.toFile().getAbsolutePath()
        ZipUtil.unzip(zipFileAbsPath, workDirAbsPath)
        FileUtils.deleteQuietly(zipFile)
        log.info("action=getPackageDir|zipPath={}|workDir={}", zipFileAbsPath, workDirAbsPath)
        return workDirAbsPath
    }

    private void apply(LaunchDeployComponentHandlerReq request, String name, String packageDir) {
        def namespace = request.getNamespaceId()
        def workloadSpecJson = ((JSONObject) request.getComponentSchema().getSpec().getWorkload().getSpec())
        def values = workloadSpecJson.getJSONObject("values")
        def valuesPath = Files.createTempFile("values", ".yaml")
        if(values != null) {
            valuesPath.write(values.toJSONString())
        }

        // 获取集群信息
        def cluster = request.getClusterId()
        def clusterConfig = clusterProvider.get(cluster).getClusterConfig()
        def token = clusterConfig.getString("oauthToken")
        def apiserver = clusterConfig.getString("masterUrl")
        def kube = clusterConfig.getString("kube")

        // 生成执行命令
        def command
        def kubeFile = null

        if (StringUtils.isNotEmpty(token) && StringUtils.isNotEmpty(apiserver)) {
            command = new String[]{
                    "/app/helm", "upgrade", "--install", name, Paths.get(packageDir, "chart"),
                    "-f", valuesPath, "-n", namespace,
                    String.format("--kube-token=%s", token),
                    String.format("--kube-apiserver=%s", apiserver),
                    "--kube-ca-file=/run/secrets/kubernetes.io/serviceaccount/ca.crt"
            }
//            command = String.format(
//                    "/app/helm upgrade --install %s %s -f %s -n %s --kube-token=%s --kube-apiserver=%s " +
//                            "--kube-ca-file=/run/secrets/kubernetes.io/serviceaccount/ca.crt ",
//                    name, Paths.get(packageDir, "chart").toString(), valuesPath, namespace, token, apiserver
//            )
        } else if (StringUtils.isNotEmpty(kube)) {
            kubeFile = Files.createTempFile("kubeconfig", ".json").toFile();
            FileUtils.writeStringToFile(kubeFile, kube, StandardCharsets.UTF_8)
            command = new String[]{
                    "/app/helm", "upgrade", "--install", name, Paths.get(packageDir, "chart"),
                    "-f", valuesPath, "-n", namespace,
                    "--kubeconfig", kubeFile.getAbsolutePath()
            }
//            command = String.format(
//                    "/app/helm upgrade --install %s %s -f %s -n %s --kubeconfig %s",
//                    name, Paths.get(packageDir, "chart").toString(), valuesPath, namespace,
//                    kubeFile.getAbsolutePath()
//            )
        } else {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find cluster authorization info|clusterId=%s", cluster))
        }

        try {
            def output = CommandUtil.runLocalCommand(command)
            log.info("action=runHelmCommand|command={}|output={}", command, output)
        } catch (AppException e) {
            if (e.getErrorMessage().contains("cannot re-use a name")) {
                def output = CommandUtil.runLocalCommand(command)
                log.info("action=runHelmCommand|command={}|output={}", command, output)
            } else {
                throw e
            }
        } finally {
            if (kubeFile != null) {
                def path = kubeFile.getAbsoluteFile()
                if (!kubeFile.delete()) {
                    log.error("cannot delete temp kubeconfig file, please check|path={}", path)
                }
            }
        }
    }

    private static String getMetaName(String appId, String componentName, String stageId) {
        def name
        if (StringUtils.isEmpty(stageId)) {
            name = String.format("%s-%s", appId, componentName)
        } else {
            name = String.format("%s-%s-%s", stageId, appId, componentName)
        }
        return name
    }
}
