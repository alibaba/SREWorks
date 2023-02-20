package com.alibaba.tesla.appmanager.server.listener;

import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.domain.core.ScriptIdentifier;
import com.alibaba.tesla.appmanager.dynamicscript.core.GroovyHandlerFactory;
import com.alibaba.tesla.appmanager.dynamicscript.repository.condition.DynamicScriptQueryCondition;
import com.alibaba.tesla.appmanager.dynamicscript.service.DynamicScriptService;
import com.alibaba.tesla.appmanager.dynamicscript.util.GroovyUtil;
import com.alibaba.tesla.appmanager.server.service.informer.InformerManager;
import com.alibaba.tesla.appmanager.spring.event.ApplicationContextLoadedEvent;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Application Context 属性加载完毕后触发 Groovy Handler Factory 的初始化
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Slf4j
@Component
public class ApplicationContextLoadedEventListener implements ApplicationListener<ApplicationContextLoadedEvent> {

    /**
     * 默认加载的 Groovy 文件
     */
    private final List<String> DEFAULT_GROOVY_SCRIPTS = Arrays.asList(
            "/dynamicscripts/DefaultBuildInternalAddonProductopsHandler.groovy",
            "/dynamicscripts/DefaultDeployInternalAddonProductopsHandler.groovy",
            "/dynamicscripts/DefaultBuildInternalAddonDevelopmentMetaHandler.groovy",
            "/dynamicscripts/DefaultDeployInternalAddonDevelopmentMetaHandler.groovy",
            "/dynamicscripts/DefaultBuildInternalAddonAppBindingHandler.groovy",
            "/dynamicscripts/DefaultDeployInternalAddonAppBindingHandler.groovy",
            "/dynamicscripts/DefaultBuildInternalAddonAppMetaHandler.groovy",
            "/dynamicscripts/DefaultDeployInternalAddonAppMetaHandler.groovy",
            "/dynamicscripts/DefaultInternalAddonV2ProductopsBuildHandler.groovy",
            "/dynamicscripts/DefaultInternalAddonV2ProductopsDeployHandler.groovy",
            "/dynamicscripts/DefaultBuildResourceAddonHandler.groovy",
            "/dynamicscripts/DefaultDeployResourceAddonHandler.groovy",
            "/dynamicscripts/TraitHostAliases.groovy",
            "/dynamicscripts/TraitHostNetwork.groovy",
            "/dynamicscripts/TraitSystemEnv.groovy",
            "/dynamicscripts/TraitNodeSelector.groovy",
            "/dynamicscripts/TraitSecret.groovy",
            "/dynamicscripts/MicroserviceComponentBuildHandler.groovy",
            "/dynamicscripts/MicroserviceComponentDeployHandler.groovy",
            "/dynamicscripts/MicroserviceComponentHandler.groovy",
            "/dynamicscripts/MicroserviceComponentDestroyHandler.groovy",
            "/dynamicscripts/MicroserviceComponentWatchKubernetesInformerHandler.groovy",
            "/dynamicscripts/JobComponentBuildHandler.groovy",
            "/dynamicscripts/JobComponentDeployHandler.groovy",
            "/dynamicscripts/JobComponentHandler.groovy",
            "/dynamicscripts/JobComponentDestroyHandler.groovy",
            "/dynamicscripts/JobComponentWatchKubernetesInformerHandler.groovy",
            "/dynamicscripts/HelmComponentHandler.groovy",
            "/dynamicscripts/HelmComponentBuildHandler.groovy",
            "/dynamicscripts/HelmComponentDeployHandler.groovy",
            "/dynamicscripts/HelmComponentDestroyHandler.groovy",
            "/dynamicscripts/ScriptComponentHandler.groovy",
            "/dynamicscripts/ScriptComponentBuildHandler.groovy",
            "/dynamicscripts/ScriptComponentDeployHandler.groovy",
            "/dynamicscripts/ScriptComponentWatchCronHandler.groovy",
            "/dynamicscripts/InternalAddonV2ProductopsComponentHandler.groovy",
            "/dynamicscripts/InternalAddonV2ProductopsComponentDestroyHandler.groovy",
            "/dynamicscripts/WorkflowDeployHandler.groovy",
            "/dynamicscripts/WorkflowRemoteDeployHandler.groovy",
            "/dynamicscripts/WorkflowSuspendHandler.groovy",
            "/dynamicscripts/WorkflowApplyComponentsHandler.groovy",
            "/dynamicscripts/WorkflowRolloutPromotionHandler.groovy",
            "/dynamicscripts/PolicyTopologyHandler.groovy",
            "/dynamicscripts/PolicyOverrideHandler.groovy",
            "/dynamicscripts/CustomStatusGenericResource.groovy"
    );

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    @Autowired
    private GroovyHandlerFactory groovyHandlerFactory;

    @Autowired
    private DynamicScriptService dynamicScriptService;

    @Override
    public void onApplicationEvent(ApplicationContextLoadedEvent event) {
        try {
            initDefaultScripts();
            groovyHandlerFactory.refresh();
        } catch (Exception e) {
            throw new AppException(AppErrorCode.UNKNOWN_ERROR, "cannot init groovy handler factory", e);
        }
    }

    private String readFileContent(String filePath) throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(Objects.requireNonNull(classLoader.getResource(filePath)).getFile());
        byte[] bytes = Files.readAllBytes(file.toPath());
        return new String(bytes);
    }

    /**
     * 搜索 resources 下的自带默认执行器
     */
    private void initDefaultScripts() {
        for (String groovyPath : DEFAULT_GROOVY_SCRIPTS) {
            ClassPathResource groovyResource = new ClassPathResource(groovyPath);
            try {
                String code;
                try {
                    code = IOUtils.toString(groovyResource.getInputStream(), StandardCharsets.UTF_8);
                } catch (FileNotFoundException e) {
                    code = readFileContent(groovyPath);
                }
                Class<?> clazz = groovyClassLoader.parseClass(code);
                ScriptIdentifier identifier = GroovyUtil.getScriptIdentifierFromClass(clazz);
                DynamicScriptQueryCondition condition = DynamicScriptQueryCondition.builder()
                        .kind(identifier.getKind())
                        .name(identifier.getName())
                        .build();
                dynamicScriptService.initScript(condition, identifier.getRevision(), code);
            } catch (Exception e) {
                throw new AppException(AppErrorCode.UNKNOWN_ERROR,
                        String.format("cannot initialize default scripts in resources directory: %s", groovyPath), e);
            }
        }
    }
}
