package com.alibaba.tesla.appmanager.deployconfig.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.deployconfig.util.DeployConfigGenerator;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@Slf4j
public class TestUtilDeployConfigGenerator {

    /**
     * 测试生成一个普通的 SpecComponent 配置
     */
    @Test
    public void testGenerate() {
        DeployConfigGenerator config = new DeployConfigGenerator();
        config.addRevisionName("HELM", "test");
        config.addScope("Namespace", "testNamespace");
        config.addScope("Stage", "testStage");
        config.addDataInputs(JSONArray.parseArray(JSONArray.toJSONString(
                Collections.singletonList(DeployAppSchema.DataInput.builder()
                        .valueFrom(DeployAppSchema.DataInputValueFrom.builder()
                                .dataOutputName("test")
                                .build())
                        .toFieldPaths(List.of("to1"))
                        .build()))));
        config.addDataOutputs(JSONArray.parseArray(JSONArray.toJSONString(
                Collections.singletonList(DeployAppSchema.DataOutput.builder()
                        .name("testname")
                        .fieldPath("testfieldpath")
                        .build()))));
        DeployAppSchema.SpecComponent specComponent = SchemaUtil
                .toSchema(DeployAppSchema.SpecComponent.class, config.toString());
        assertThat(specComponent.getNamespaceId()).isEqualTo("testNamespace");
        assertThat(specComponent.getStageId()).isEqualTo("testStage");
        assertThat(specComponent.getRevisionName()).isEqualTo("HELM|test|_");
        assertThat(specComponent.getDataInputs().get(0).getValueFrom().getDataOutputName()).isEqualTo("test");
        assertThat(specComponent.getDataOutputs().get(0).getFieldPath()).isEqualTo("testfieldpath");
    }
}
