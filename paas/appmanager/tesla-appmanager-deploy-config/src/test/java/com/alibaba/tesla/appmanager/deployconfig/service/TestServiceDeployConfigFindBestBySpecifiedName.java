package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.service.GitService;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigHistoryRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.DeployConfigRepository;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.deployconfig.service.impl.DeployConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Slf4j
public class TestServiceDeployConfigFindBestBySpecifiedName {

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

    @Test
    public void test() throws Exception {
        String configPath = (new ClassPathResource("fixtures/deploy_config_records/empty_env_id_app_records.json"))
                .getFile()
                .getAbsolutePath();
        String config = new String(Files.readAllBytes(Paths.get(configPath)), StandardCharsets.UTF_8);
        List<DeployConfigDO> appRecords = JSONArray.parseArray(config, DeployConfigDO.class);
        List<DeployConfigDO> rootRecords = new ArrayList<>();
        List<DeployConfigDO> filterAppRecords = appRecords.stream()
                .filter(item -> item.getTypeId().equals("Type:components::ComponentType:K8S_MICROSERVICE::ComponentName:server"))
                .collect(Collectors.toList());
        DeployConfigDO best = deployConfigService.findBestConfigInRecordsBySpecifiedName(
                filterAppRecords, rootRecords, rootRecords, "230E", null, "apsara-bigdata-manager", "master");
        assertThat(best).isNotNull();
        log.info("best: {}", JSONObject.toJSONString(best));
        assertThat(best.getTypeId()).isEqualTo("Type:components::ComponentType:K8S_MICROSERVICE::ComponentName:server");
    }
}
