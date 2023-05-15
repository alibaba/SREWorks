package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigHistoryRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigRepository;
import com.alibaba.tesla.appmanager.deployconfig.service.impl.DeployConfigServiceImpl;
import com.alibaba.tesla.appmanager.domain.req.deployconfig.DeployConfigSyncToGitBaselineReq;
import com.alibaba.tesla.appmanager.domain.res.deployconfig.DeployConfigSyncToGitBaselineRes;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 本文件用于测试 DeployConfig 中同步到 Git 指定基线仓库中的逻辑
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@RunWith(SpringRunner.class)
@Slf4j
public class TestServiceDeployConfigSyncToGitBaseline {
    
    private static final String CI_ACCOUNT = "CI_ACCOUNT";
    private static final String CI_TOKEN = "CI_TOKEN";
    private static final String BRANCH = "BRANCH";
    private static final String REPO = "REPO";
    private static final String FILEPATH = "testapp/launch.yaml";
    private static final String OPERATOR = "SYSTEM";

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
     * 测试远端空文件
     */
    @Test
    public void testEmptyRemoteFile() throws Exception {
        Resource localResource = new ClassPathResource("fixtures/application_configuration/sync_to_git_baseline_local.yaml");
        String localConfig = Files.readString(Paths.get(localResource.getFile().getAbsolutePath()));
        DeployAppSchema localSchema = SchemaUtil.toSchema(DeployAppSchema.class, localConfig);
        DeployConfigSyncToGitBaselineReq request = DeployConfigSyncToGitBaselineReq.builder()
                .configuration(localSchema)
                .ciAccount(CI_ACCOUNT)
                .ciToken(CI_TOKEN)
                .branch(BRANCH)
                .repo(REPO)
                .filePath(FILEPATH)
                .operator(OPERATOR)
                .build();
        DeployConfigSyncToGitBaselineRes res = deployConfigService.syncToGitBaseline(request);
        log.info("res: {}", SchemaUtil.toYamlMapStr(res.getSchema()));
        assertThat(res.getSchema().getSpec().getComponents().size()).isEqualTo(2);
        assertThat(res.getSchema().getSpec().getParameterValues().size()).isEqualTo(3);
    }

    @Test
    public void testReplaceRemoteFile() throws Exception {
        Resource localResource = new ClassPathResource("fixtures/application_configuration/sync_to_git_baseline_local.yaml");
        Resource remoteResource = new ClassPathResource("fixtures/application_configuration/sync_to_git_baseline_remote.yaml");
        String localConfig = Files.readString(Paths.get(localResource.getFile().getAbsolutePath()));
        String remoteConfig = Files.readString(Paths.get(remoteResource.getFile().getAbsolutePath()));
        DeployAppSchema localSchema = SchemaUtil.toSchema(DeployAppSchema.class, localConfig);
        DeployConfigSyncToGitBaselineReq request = DeployConfigSyncToGitBaselineReq.builder()
                .configuration(localSchema)
                .ciAccount(CI_ACCOUNT)
                .ciToken(CI_TOKEN)
                .branch(BRANCH)
                .repo(REPO)
                .filePath(FILEPATH)
                .operator(OPERATOR)
                .build();
        Mockito.doAnswer(invocation -> {
            final Object[] args = invocation.getArguments();
            Path cloneDir = (Path) args[2];
            FileUtils.writeStringToFile(Paths.get(cloneDir.toString(), FILEPATH).toFile(), remoteConfig, StandardCharsets.UTF_8);
            return null;
        }).when(gitService).cloneRepo(Mockito.any(), Mockito.any(), Mockito.any());
        DeployConfigSyncToGitBaselineRes res = deployConfigService.syncToGitBaseline(request);
        log.info("res: {}", SchemaUtil.toYamlMapStr(res.getSchema()));
        assertThat(res.getSchema().getSpec().getComponents().size()).isEqualTo(2);
        assertThat(res.getSchema().getSpec().getComponents().get(0).getParameterValue("TARGET_ENDPOINT")).isEqualTo("SET_BY_USER");
        assertThat(res.getSchema().getSpec().getParameterValues().size()).isEqualTo(2);
    }
}
