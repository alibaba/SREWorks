package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.deployconfig.assembly.DeployConfigDtoConvert;
import com.alibaba.tesla.appmanager.deployconfig.repository.domain.DeployConfigDO;
import com.alibaba.tesla.appmanager.domain.dto.DeployConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Slf4j
public class TestDeployConfigDtoConverter {

    @Test
    public void testTo1() {
        String yaml = "!!map\n" +
                "- name: a\n" +
                "  value: b\n" +
                "- name: c\n" +
                "  value: d";
        DeployConfigDO record = new DeployConfigDO();
        record.setConfig(yaml);
        DeployConfigDTO dto = (new DeployConfigDtoConvert()).to(record);
        assertThat(((JSONArray) dto.getConfigJson()).size()).isEqualTo(2);
    }

    @Test
    public void testTo2() {
        String yaml = "- name: c\n" +
                "  value: d";
        DeployConfigDO record = new DeployConfigDO();
        record.setConfig(yaml);
        DeployConfigDTO dto = (new DeployConfigDtoConvert()).to(record);
        assertThat(((JSONArray) dto.getConfigJson()).size()).isEqualTo(1);
    }

    @Test
    public void testTo3() {
        String yaml = "name: c\n" +
                "value: d";
        DeployConfigDO record = new DeployConfigDO();
        record.setConfig(yaml);
        DeployConfigDTO dto = (new DeployConfigDtoConvert()).to(record);
        assertThat(((JSONObject) dto.getConfigJson()).get("name")).isEqualTo("c");
        assertThat(((JSONObject) dto.getConfigJson()).get("value")).isEqualTo("d");
    }
}
