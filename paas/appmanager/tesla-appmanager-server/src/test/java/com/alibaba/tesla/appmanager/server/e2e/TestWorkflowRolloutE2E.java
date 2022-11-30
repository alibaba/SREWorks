package com.alibaba.tesla.appmanager.server.e2e;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.tesla.appmanager.api.provider.WorkflowInstanceProvider;
import com.alibaba.tesla.appmanager.common.constants.WorkflowContextKeyConstant;
import com.alibaba.tesla.appmanager.common.enums.WorkflowInstanceStateEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.common.util.SchemaUtil;
import com.alibaba.tesla.appmanager.domain.dto.WorkflowInstanceDTO;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import com.alibaba.tesla.appmanager.domain.schema.DeployAppSchema;
import com.alibaba.tesla.appmanager.kubernetes.KubernetesClientFactory;
import com.alibaba.tesla.appmanager.server.TestApplication;
import com.alibaba.tesla.appmanager.spring.util.FixtureUtil;
import com.google.common.base.Enums;
import io.fabric8.kubernetes.api.model.apps.StatefulSet;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;

import static org.assertj.core.api.AssertionsForClassTypes.*;

/**
 * 测试 Workflow 组件灰度回滚流程 E2E
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@SpringBootConfiguration
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TestWorkflowRolloutE2E {

    public static final String APP_ID = "testapp-rollout";
    public static final String OPERATOR = "122592";

    @Autowired
    private WorkflowInstanceProvider workflowInstanceProvider;

    @Autowired
    private KubernetesClientFactory kubernetesClientFactory;

    /**
     * 测试灰度流程 - 直接确认灰度结果并继续部署到终态
     */
    @Test
    public void testContinuous() throws Exception {
        WorkflowInstanceDTO res = launch("application_configuration/e2e_workflow_rollout.yaml");
        for (int i = 0; i < 30; i++) {
            WorkflowInstanceDTO result = workflowInstanceProvider.get(res.getId(), true);
            WorkflowInstanceStateEnum status = Enums
                    .getIfPresent(WorkflowInstanceStateEnum.class, result.getWorkflowStatus()).orNull();
            assertThat(status).isNotNull();
            switch (status) {
                case FAILURE:
                case EXCEPTION:
                case TERMINATED:
                    throw new AppException(AppErrorCode.DEPLOY_ERROR,
                            String.format("test launch workflow failed||order=%s", JSONObject.toJSONString(result)));
                case SUSPEND: {
                    // 确认当前 statefulset 已经被置为 partition==1
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(1);

                    // 继续 rolling
                    JSONObject context = new JSONObject();
                    context.put("continueRollout", true);
                    context.put("rollback", false);
                    workflowInstanceProvider.putContext(res.getId(), context);
                    log.info("rollout context has put into workflow context|workflowInstanceId={}|context={}",
                            res.getId(), JSONObject.toJSONString(context));
                    workflowInstanceProvider.resume(res.getId());
                    break;
                }
                case SUCCESS: {
                    // 确认当前 statefulset 已经被置为 partition==0
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(0);
                    return;
                }
                default:
                    break;
            }
            Thread.sleep(5000);
        }
    }

    /**
     * 测试灰度流程 - 直接确认灰度结果并终止当前流程
     */
    @Test
    public void testTerminate() throws Exception {
        WorkflowInstanceDTO res = launch("application_configuration/e2e_workflow_rollout.yaml");
        for (int i = 0; i < 30; i++) {
            WorkflowInstanceDTO result = workflowInstanceProvider.get(res.getId(), true);
            WorkflowInstanceStateEnum status = Enums
                    .getIfPresent(WorkflowInstanceStateEnum.class, result.getWorkflowStatus()).orNull();
            assertThat(status).isNotNull();
            switch (status) {
                case FAILURE:
                case EXCEPTION:
                case SUCCESS:
                    throw new AppException(AppErrorCode.DEPLOY_ERROR,
                            String.format("test launch workflow failed||order=%s", JSONObject.toJSONString(result)));
                case TERMINATED: {
                    // 确认当前 statefulset 还是 partition==1
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(1);
                    return;
                }
                case SUSPEND: {
                    // 确认当前 statefulset 已经被置为 partition==1
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(1);

                    // 测试直接终止
                    JSONObject context = new JSONObject();
                    context.put("continueRollout", false);
                    context.put("rollback", false);
                    workflowInstanceProvider.putContext(res.getId(), context);
                    log.info("rollout context has put into workflow context|workflowInstanceId={}|context={}",
                            res.getId(), JSONObject.toJSONString(context));
                    workflowInstanceProvider.resume(res.getId());
                    break;
                }
                default:
                    break;
            }
            Thread.sleep(5000);
        }
    }

    /**
     * 测试灰度流程 - 回滚到上一次成功的部署流程
     */
    @Test
    public void testRollback() throws Exception {
        // 先做一次成功的部署
        testContinuous();

        WorkflowInstanceDTO res = launch("application_configuration/e2e_workflow_rollout.yaml");
        for (int i = 0; i < 30; i++) {
            WorkflowInstanceDTO result = workflowInstanceProvider.get(res.getId(), true);
            WorkflowInstanceStateEnum status = Enums
                    .getIfPresent(WorkflowInstanceStateEnum.class, result.getWorkflowStatus()).orNull();
            assertThat(status).isNotNull();
            switch (status) {
                case FAILURE:
                case EXCEPTION:
                case SUCCESS:
                    throw new AppException(AppErrorCode.DEPLOY_ERROR,
                            String.format("test launch workflow failed||order=%s", JSONObject.toJSONString(result)));
                case TERMINATED: {
                    // 确认当前 statefulset 已经被置为 partition==0
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(0);
                    WorkflowInstanceDTO instance = workflowInstanceProvider.get(res.getId(), true);
                    assertThat(instance.getWorkflowContext().get(WorkflowContextKeyConstant.CANCEL_EXECUTION)).isEqualTo(true);
                    return;
                }
                case SUSPEND: {
                    // 确认当前 statefulset 已经被置为 partition==1
                    DefaultKubernetesClient client = kubernetesClientFactory.getByKubeConfig(System.getenv("KUBECONFIG"));
                    StatefulSet sts = client.apps()
                            .statefulSets()
                            .inNamespace("abm-daily")
                            .withName("appmanager-helm-e2e-rollout-helm-demo-chart")
                            .get();
                    Integer partition = sts.getSpec().getUpdateStrategy().getRollingUpdate().getPartition();
                    assertThat(partition).isEqualTo(1);

                    // 执行回滚动作
                    JSONObject context = new JSONObject();
                    context.put("continueRollout", false);
                    context.put("rollback", true);
                    workflowInstanceProvider.putContext(res.getId(), context);
                    log.info("rollout context has put into workflow context|workflowInstanceId={}|context={}",
                            res.getId(), JSONObject.toJSONString(context));
                    workflowInstanceProvider.resume(res.getId());
                    break;
                }
                default:
                    break;
            }
            Thread.sleep(5000);
        }
    }

    /**
     * 发起服务部署 (使用环境变量 KUBECONFIG 进行连接)
     */
    private WorkflowInstanceDTO launch(String filename) throws Exception {
        String ac = FixtureUtil.getFixture(filename);
        DeployAppSchema schema = SchemaUtil.toSchema(DeployAppSchema.class, ac);
        schema.getSpec().setParameterValues(new ArrayList<>());
        schema.getSpec().getParameterValues().add(DeployAppSchema.ParameterValue.builder()
                .name("KUBECONFIG")
                .value(System.getenv("KUBECONFIG"))
                .build());
        return workflowInstanceProvider.launch(APP_ID, SchemaUtil.toYamlMapStr(schema),
                WorkflowInstanceOption.builder()
                        .category("DEPLOY")
                        .creator(OPERATOR)
                        .build());
    }
}
