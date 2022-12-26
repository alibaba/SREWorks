package com.alibaba.tesla.appmanager.server.domain;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.common.enums.WorkflowExecuteModeEnum;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.domain.schema.WorkflowGraph;
import com.alibaba.tesla.appmanager.spring.util.FixtureUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
public class TestWorkflowGraph {

    /**
     * 测试可以通过 Edges 正常构图
     */
    @Test
    public void testBuildGraph() throws Exception {
        String config = FixtureUtil.getFixture("application_configuration/test_workflow_graph.yaml");
        DeployAppSchema schema = SchemaUtil.toSchema(DeployAppSchema.class, config);
        WorkflowGraph graph = WorkflowGraph.valueOf(schema, WorkflowInstanceOption.builder()
                .mode(WorkflowExecuteModeEnum.DAG)
                .build());
        log.info("graph: {}", JSONObject.toJSONString(graph));
        assertThat(graph.getEdges().size()).isEqualTo(3);
        assertThat(graph.getReverseEdges().size()).isEqualTo(2);
        assertThat(graph.getNodes().size()).isEqualTo(4);
        assertThat(graph.getDegree().get("Type:apply-components::Name:deploy-env-e2e-1")).isEqualTo(2);
        assertThat(graph.getDegree().get("Type:apply-components::Name:deploy-env-e2e-2")).isEqualTo(0);
        assertThat(graph.getDegree().get("Type:apply-components::Name:deploy-env-e2e-3")).isEqualTo(0);
        assertThat(graph.getDegree().get("Type:apply-components::Name:deploy-env-e2e-4")).isEqualTo(1);
    }
}
