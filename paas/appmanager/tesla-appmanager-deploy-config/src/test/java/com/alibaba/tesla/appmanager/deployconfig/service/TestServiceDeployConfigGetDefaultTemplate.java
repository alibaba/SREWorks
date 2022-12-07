package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigHistoryRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.service.impl.DeployConfigServiceImpl;
import com.alibaba.tesla.appmanager.domain.container.AppComponentLocationContainer;
import com.alibaba.tesla.appmanager.domain.container.DeployAppRevisionName;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigApplyTemplateReq;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigGetDefaultTemplateReq;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 本文件用于测试 DeployConfig 中指定应用的默认模板生成
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@RunWith(SpringRunner.class)
@Slf4j
public class TestServiceDeployConfigGetDefaultTemplate {

    private static final String APP_ID = "testapp";
    private static final String API_VERSION = DefaultConstant.API_VERSION_V1_ALPHA2;
    private static final String ENV_ID = "Namespace:abm-daily";
    private static final String ISOLATE_NAMESPACE_ID = "default";
    private static final String ISOLATE_STAGE_ID = "pre";
    private static final String UNIT_ID = "internal";
    private static final String CLUSTER_ID = "master";
    private static final String NAMESPACE_ID = "default";
    private static final String STAGE_ID = "prod";

    @Mock
    private DeployConfigRepository deployConfigRepository;

    @Mock
    private DeployConfigHistoryRepository deployConfigHistoryRepository;

    @Mock
    private GitService gitService;

    private DeployConfigService deployConfigService;

    @Before
    public void before() {
        deployConfigService = Mockito.spy(new DeployConfigServiceImpl(
                deployConfigRepository,
                deployConfigHistoryRepository,
                gitService
        ));
    }

    /**
     * 测试系统组件的默认模板生成
     */
    @Test
    public void testOnlySystemComponents() throws Exception {
        Resource config = new ClassPathResource("fixtures/application_configuration/global_template_only_system_components.yaml");
        String applicationConfiguration = Files.readString(Paths.get(config.getFile().getAbsolutePath()));
        List<DeployConfigDO> deployConfigItems = deployConfigService.applyTemplate(
                        DeployConfigApplyTemplateReq.builder()
                                .appId("")
                                .apiVersion(API_VERSION)
                                .config(applicationConfiguration)
                                .enabled(true)
                                .build())
                .getItems();
        Mockito.doReturn(deployConfigItems)
                .when(deployConfigService)
                .getGlobalTemplate(API_VERSION, ENV_ID, ISOLATE_NAMESPACE_ID, ISOLATE_STAGE_ID);
        DeployAppSchema res = deployConfigService.getDefaultTemplate(DeployConfigGetDefaultTemplateReq.builder()
                .apiVersion(API_VERSION)
                .envId(ENV_ID)
                .isolateNamespaceId(ISOLATE_NAMESPACE_ID)
                .isolateStageId(ISOLATE_STAGE_ID)
                .appId(APP_ID)
                .unitId(UNIT_ID)
                .clusterId(CLUSTER_ID)
                .namespaceId(NAMESPACE_ID)
                .stageId(STAGE_ID)
                .build());
        log.info("res: {}", JSONObject.toJSONString(res));
        assertThat(res.getSpec().getComponents().size()).isEqualTo(1);
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(0).getRevisionName()).getComponentType()).isEqualTo("INTERNAL_ADDON");
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(0).getRevisionName()).getComponentName()).isEqualTo("productops");
    }

    /**
     * 测试用户组件的默认模板生成
     */
    @Test
    public void testOnlyUserComponents() throws Exception {
        Resource config = new ClassPathResource("fixtures/application_configuration/global_template_only_user_components.yaml");
        String applicationConfiguration = Files.readString(Paths.get(config.getFile().getAbsolutePath()));
        List<DeployConfigDO> deployConfigItems = deployConfigService.applyTemplate(
                DeployConfigApplyTemplateReq.builder()
                        .appId("")
                        .apiVersion(API_VERSION)
                        .config(applicationConfiguration)
                        .enabled(true)
                        .build())
                .getItems();
        Mockito.doReturn(deployConfigItems)
                .when(deployConfigService)
                .getGlobalTemplate(API_VERSION, ENV_ID, ISOLATE_NAMESPACE_ID, ISOLATE_STAGE_ID);
        DeployAppSchema res = deployConfigService.getDefaultTemplate(DeployConfigGetDefaultTemplateReq.builder()
                .apiVersion(API_VERSION)
                .envId(ENV_ID)
                .isolateNamespaceId(ISOLATE_NAMESPACE_ID)
                .isolateStageId(ISOLATE_STAGE_ID)
                .appId(APP_ID)
                .unitId(UNIT_ID)
                .clusterId(CLUSTER_ID)
                .namespaceId(NAMESPACE_ID)
                .stageId(STAGE_ID)
                .appComponents(Arrays.asList(AppComponentLocationContainer.builder()
                        .componentType("K8S_MICROSERVICE")
                        .componentName("A")
                        .build(), AppComponentLocationContainer.builder()
                        .componentType("K8S_MICROSERVICE")
                        .componentName("B")
                        .build()))
                .build());
        log.info("res: {}", JSONObject.toJSONString(res));
        assertThat(res.getSpec().getComponents().size()).isEqualTo(2);
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(0).getRevisionName()).getComponentName()).isEqualTo("A");
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(1).getRevisionName()).getComponentName()).isEqualTo("B");
    }

    /**
     * 测试混合组件的默认模板生成
     */
    @Test
    public void testMixComponents() throws Exception {
        Resource config = new ClassPathResource("fixtures/application_configuration/global_template_mix_components.yaml");
        String applicationConfiguration = Files.readString(Paths.get(config.getFile().getAbsolutePath()));
        List<DeployConfigDO> deployConfigItems = deployConfigService.applyTemplate(
                        DeployConfigApplyTemplateReq.builder()
                                .appId("")
                                .apiVersion(API_VERSION)
                                .config(applicationConfiguration)
                                .enabled(true)
                                .build())
                .getItems();
        Mockito.doReturn(deployConfigItems)
                .when(deployConfigService)
                .getGlobalTemplate(API_VERSION, ENV_ID, ISOLATE_NAMESPACE_ID, ISOLATE_STAGE_ID);
        DeployAppSchema res = deployConfigService.getDefaultTemplate(DeployConfigGetDefaultTemplateReq.builder()
                .apiVersion(API_VERSION)
                .envId(ENV_ID)
                .isolateNamespaceId(ISOLATE_NAMESPACE_ID)
                .isolateStageId(ISOLATE_STAGE_ID)
                .appId(APP_ID)
                .unitId(UNIT_ID)
                .clusterId(CLUSTER_ID)
                .namespaceId(NAMESPACE_ID)
                .stageId(STAGE_ID)
                .appComponents(Arrays.asList(AppComponentLocationContainer.builder()
                        .componentType("K8S_MICROSERVICE")
                        .componentName("A")
                        .build(), AppComponentLocationContainer.builder()
                        .componentType("K8S_MICROSERVICE")
                        .componentName("B")
                        .build()))
                .build());
        log.info("res: {}", JSONObject.toJSONString(res));
        assertThat(res.getSpec().getComponents().size()).isEqualTo(3);
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(0).getRevisionName()).getComponentType()).isEqualTo("INTERNAL_ADDON");
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(0).getRevisionName()).getComponentName()).isEqualTo("productops");
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(1).getRevisionName()).getComponentName()).isEqualTo("A");
        assertThat(DeployAppRevisionName.valueOf(res.getSpec().getComponents().get(2).getRevisionName()).getComponentName()).isEqualTo("B");
    }
}
