package com.alibaba.tesla.appmanager.domain.schema;

import com.alibaba.tesla.appmanager.common.enums.WorkflowExecuteModeEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.container.WorkflowGraphNodeId;
import com.alibaba.tesla.appmanager.domain.option.WorkflowInstanceOption;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Workflow 实例 Graph 存储
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkflowGraph implements Serializable {

    /**
     * 图邻接表存储
     */
    private Map<String, List<String>> edges;

    /**
     * 反向图邻接表存储
     */
    private Map<String, List<String>> reverseEdges;

    /**
     * 节点列表
     */
    private List<String> nodes;

    /**
     * 节点入度
     */
    private Map<String, Integer> degree;

    /**
     * 反向节点入度 (用于反向追踪)
     */
    private Map<String, Integer> reverseDegree;

    private final Object lock = new Object();

    /**
     * 获取当前所有入度为 0 的节点列表
     *
     * @return List of NodeId
     */
    public List<String> getZeroDegreeNodes() {
        synchronized (lock) {
            if (degree == null) {
                return new ArrayList<>();
            }
            return degree.keySet().stream()
                    .filter(item -> degree.get(item) == 0 && nodes.contains(item))
                    .collect(Collectors.toList());
        }
    }

    /**
     * 获取当前所有反向入度为 0 的节点列表
     *
     * @return List of NodeId
     */
    public List<String> getReverseZeroDegreeNodes() {
        if (reverseDegree == null) {
            return new ArrayList<>();
        }
        return reverseDegree.keySet().stream()
                .filter(item -> reverseDegree.get(item) == 0)
                .collect(Collectors.toList());
    }

    /**
     * 在入度 Map 中删除所有
     *
     * @param nodeId 节点 ID
     */
    public void removeNodeInDegree(String nodeId) {
        synchronized (lock) {
            List<String> outs = edges.get(nodeId);
            if (outs == null) {
                throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                        String.format("invalid nodeId to remove degress|nodeId=%s", nodeId));
            }
            outs.forEach(out -> {
                int current = degree.get(out);
                if (current <= 0) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("conflict workflow graph, invalid degree operation|nodeId=%s", nodeId));
                }
                degree.put(out, current - 1);
            });
            nodes.remove(nodeId);
        }
    }

    /**
     * 将用户提交的 schema 进行构图，转换为 WorkflowGraph 返回
     *
     * @param schema Application Configuration
     * @param option 运行选项
     * @return WorkflowGraph
     */
    public static WorkflowGraph valueOf(DeployAppSchema schema, WorkflowInstanceOption option) {
        WorkflowExecuteModeEnum mode = option.getMode();
        if (mode == null) {
            mode = WorkflowExecuteModeEnum.STEP_BY_STEP;
        }

        WorkflowGraph graph = new WorkflowGraph();
        graph.setEdges(new HashMap<>());
        graph.setReverseEdges(new HashMap<>());
        graph.setNodes(new ArrayList<>());
        graph.setDegree(new HashMap<>());
        graph.setReverseDegree(new HashMap<>());

        List<DeployAppSchema.WorkflowStep> steps = schema.getSpec().getWorkflow().getSteps();

        // 添加所有 task 节点 / 输入输出变量节点
        Map<String, String> outputVarMap = new HashMap<>();
        steps.forEach(step -> {
            String type = step.getType();
            String name = step.getName();
            String currentNodeId = (new WorkflowGraphNodeId(type, name)).toString();
            graph.getNodes().add(currentNodeId);
            graph.getDegree().putIfAbsent(currentNodeId, 0);
            graph.getReverseDegree().putIfAbsent(currentNodeId, 0);
            if (step.getInputs() != null) {
                step.getInputs().forEach(input -> {
                    if (StringUtils.isAnyEmpty(input.getFrom(), input.getParameterKey())) {
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                "invalid input properties in workflow step");
                    }
                });
            }
            if (step.getOutputs() != null) {
                step.getOutputs().forEach(output -> {
                    String var = output.getName();
                    if (StringUtils.isAnyEmpty(var, output.getValueFrom())) {
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                "invalid output properties in workflow steps");
                    }
                    if (outputVarMap.containsKey(var)) {
                        throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                                String.format("multiple output found for var %s in workflow steps", var));
                    }
                    outputVarMap.put(var, currentNodeId);
                });
            }
        });

        // 对整个 workflow 的 DAG 进行连边
        for (int i = 0; i < steps.size(); i++) {
            DeployAppSchema.WorkflowStep step = steps.get(i);
            String type = step.getType();
            String name = step.getName();
            String currentNodeId = (new WorkflowGraphNodeId(type, name)).toString();

            // 对变量依赖进行连边
            step.getInputs().forEach(input -> {
                String var = input.getFrom();
                if (!outputVarMap.containsKey(var)) {
                    throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                            String.format("cannot find the input source of var %s in workflow steps", var));
                }
                String fromNodeId = outputVarMap.get(var);
                graph.getEdges().putIfAbsent(fromNodeId, new ArrayList<>());
                graph.getEdges().get(fromNodeId).add(currentNodeId);
                graph.getReverseEdges().putIfAbsent(currentNodeId, new ArrayList<>());
                graph.getReverseEdges().get(currentNodeId).add(fromNodeId);
            });

            // 顺次执行的情况下，上下节点进行连边
            if (i > 0 && mode == WorkflowExecuteModeEnum.STEP_BY_STEP) {
                DeployAppSchema.WorkflowStep prevStep = steps.get(i - 1);
                String fromNodeId = (new WorkflowGraphNodeId(prevStep.getType(), prevStep.getName())).toString();
                String toNodeId = (new WorkflowGraphNodeId(type, name)).toString();
                graph.getEdges().putIfAbsent(fromNodeId, new ArrayList<>());
                graph.getEdges().get(fromNodeId).add(toNodeId);
                graph.getReverseEdges().putIfAbsent(toNodeId, new ArrayList<>());
                graph.getReverseEdges().get(toNodeId).add(fromNodeId);
            }
        }

        // 对完成后的邻接表进行去重
        for (String key : graph.getEdges().keySet()) {
            graph.getEdges().put(key,
                    graph.getEdges().get(key).stream().distinct().collect(Collectors.toList()));
        }
        for (String key : graph.getReverseEdges().keySet()) {
            graph.getReverseEdges().put(key,
                    graph.getReverseEdges().get(key).stream().distinct().collect(Collectors.toList()));
        }

        // 设置入度 Map
        graph.getEdges().forEach((key, value) -> {
            for (String toNodeId : value) {
                graph.getDegree().put(toNodeId, graph.getDegree().get(toNodeId) + 1);
            }
        });

        // 设置反向入度 Map
        graph.getReverseEdges().forEach((key, value) -> {
            for (String toNodeId : value) {
                graph.getReverseDegree().put(toNodeId, graph.getReverseDegree().get(toNodeId) + 1);
            }
        });
        return graph;
    }
}
