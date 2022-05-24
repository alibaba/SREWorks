package com.alibaba.tesla.appmanager.server.dag.scenes;

import com.alibaba.tesla.appmanager.common.constants.AppFlowParamKey;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.dag.local.AbstractLocalDagBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DeployAppComponentRunnerScene extends AbstractLocalDagBase {

    public static String name = "deploy_app_component_runner";

    @Override
    public void draw() throws Exception {
        node("DeployAppDeciderNode");
        node("DeployAppCreateComponentNode");
        node("DeployAppCreateResourceAddonNode");
        node("DeployAppCreateCustomAddonNode");
        node("DeployAppWaitComponentNode");
        node("DeployAppWaitAddonNode");
        node("DeployAppWaitCustomAddonNode");
        node("DeployAppTraitNode");

        edge("DeployAppDeciderNode", "DeployAppCreateComponentNode",
                String.format("#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s' || " +
                                "#DeployAppDeciderNode['output']['%s'] == '%s'",
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.K8S_MICROSERVICE,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.INTERNAL_ADDON,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.ABM_CHART,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.HELM,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.ABM_OPERATOR_TVD,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.ABM_KUSTOMIZE,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.ABM_HELM,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.ASI_COMPONENT,
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.K8S_JOB));
        edge("DeployAppDeciderNode", "DeployAppCreateResourceAddonNode",
                String.format("#DeployAppDeciderNode['output']['%s'] == '%s'",
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.RESOURCE_ADDON));
        edge("DeployAppDeciderNode", "DeployAppTraitNode",
                String.format("#DeployAppDeciderNode['output']['%s'] == '%s'",
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.TRAIT_ADDON));
        edge("DeployAppDeciderNode", "DeployAppCreateCustomAddonNode",
                String.format("#DeployAppDeciderNode['output']['%s'] == '%s'",
                        AppFlowParamKey.COMPONENT_TYPE,
                        ComponentTypeEnum.CUSTOM_ADDON));

        edge("DeployAppCreateComponentNode", "DeployAppWaitComponentNode");
        edge("DeployAppCreateResourceAddonNode", "DeployAppWaitAddonNode");
        edge("DeployAppCreateCustomAddonNode", "DeployAppWaitCustomAddonNode");
    }
}
