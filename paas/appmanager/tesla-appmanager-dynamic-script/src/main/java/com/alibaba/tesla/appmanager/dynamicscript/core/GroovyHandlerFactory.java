package com.alibaba.tesla.appmanager.dynamicscript.core;

import com.alibaba.tesla.appmanager.common.constants.DefaultConstant;
import com.alibaba.tesla.appmanager.common.enums.ComponentActionEnum;
import com.alibaba.tesla.appmanager.common.enums.ComponentTypeEnum;
import com.alibaba.tesla.appmanager.common.enums.DynamicScriptKindEnum;
import com.alibaba.tesla.appmanager.common.exception.AppErrorCode;
import com.alibaba.tesla.appmanager.common.exception.AppException;
import com.alibaba.tesla.appmanager.dynamicscript.repository.condition.DynamicScriptQueryCondition;
import com.alibaba.tesla.appmanager.dynamicscript.repository.domain.DynamicScriptDO;
import com.alibaba.tesla.appmanager.dynamicscript.service.DynamicScriptService;
import com.alibaba.tesla.appmanager.server.dynamicscript.handler.ComponentHandler;
import com.alibaba.tesla.appmanager.spring.util.SpringBeanUtil;
import groovy.lang.GroovyClassLoader;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.tesla.appmanager.common.constants.DefaultConstant.INTERNAL_ADDON_DEVELOPMENT_META;

@Component
@Slf4j
public class GroovyHandlerFactory {

    /**
     * Handler 版本映射
     */
    private static final ConcurrentHashMap<String, Integer> VERSION_MAP = new ConcurrentHashMap<>();

    /**
     * Handler 实例缓存
     */
    private static final ConcurrentHashMap<String, GroovyHandler> HANDLER_INSTANCES = new ConcurrentHashMap<>();

    private final GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    @Autowired
    private DynamicScriptService dynamicScriptService;

    /**
     * 定时检查并刷新 Groovy Handler
     */
    @Scheduled(cron = "${appmanager.cron-job.groovy-handler-factory-refresh}")
    public void refresh() {
        List<DynamicScriptDO> scripts = dynamicScriptService.list(DynamicScriptQueryCondition.builder().build());
        for (DynamicScriptDO script : scripts) {
            String kind = script.getKind();
            String name = script.getName();
            String key = keyGenerator(kind, name);
            Integer revision = script.getCurrentRevision();
            if (!VERSION_MAP.containsKey(key) || !revision.equals(VERSION_MAP.getOrDefault(key, -1))) {
                synchronized (GroovyHandlerFactory.class) {
                    try {
                        createOrUpdate(kind, name);
                    } catch (AppException e) {
                        log.error("cannot refresh groovy handler|message={}", e.getErrorMessage());
                    }
                }
            } else {
                log.debug("no need to refresh groovy handler|kind={}|name={}", kind, name);
            }
        }
    }

    /**
     * 获取当前全量的 Groovy Handler 列表
     *
     * @return GroovyHandlerItem List 对象
     */
    public List<GroovyHandlerItem> list() {
        List<GroovyHandlerItem> results = new ArrayList<>();
        for (Map.Entry<String, GroovyHandler> entry : HANDLER_INSTANCES.entrySet()) {
            KeyStore keyStore = keySplit(entry.getKey());
            results.add(GroovyHandlerItem.builder()
                    .kind(keyStore.getKind())
                    .name(keyStore.getName())
                    .groovyHandler(entry.getValue())
                    .build());
        }
        return results;
    }

    /**
     * 判断指定的 kind+name 对应的 Groovy 脚本是否存在
     *
     * @param kind 类型
     * @param name 名称
     * @return true or flase
     */
    public boolean exists(String kind, String name) {
        if (Objects.isNull(kind) || StringUtils.isEmpty(name)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "name cannot be empty");
        }

        String key = keyGenerator(kind, name);
        return HANDLER_INSTANCES.containsKey(key);
    }

    /**
     * 获取 Handler
     *
     * @param scriptClass 脚本 Class
     * @param kind        动态脚本类型
     * @param name        动态脚本唯一标识
     * @return Handler
     */
    public <T extends GroovyHandler> T get(Class<T> scriptClass, String kind, String name) {
        if (Objects.isNull(kind) || StringUtils.isEmpty(name)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "name cannot be empty");
        }

        // 先从缓存获取
        String key = keyGenerator(kind, name);
        GroovyHandler groovyHandler = HANDLER_INSTANCES.get(key);
        if (groovyHandler != null) {
            return scriptClass.cast(groovyHandler);
        }

        synchronized (GroovyHandlerFactory.class) {
            groovyHandler = HANDLER_INSTANCES.get(key);
            if (groovyHandler != null) {
                return scriptClass.cast(groovyHandler);
            }
            groovyHandler = createOrUpdate(kind, name);
        }
        return scriptClass.cast(groovyHandler);
    }

    /**
     * 根据 ComponentType 获取 Handler
     *
     * @param scriptClass   脚本 Class
     * @param appId         应用 ID (TODO: 后续每个应用可以自己自定义逻辑)
     * @param componentType 组件类型
     * @param componentName 组件名称
     * @return Handler 如果不支持，返回 null
     */
    public <T extends GroovyHandler> T getByComponentType(
            Class<T> scriptClass, String appId, String componentType, String componentName,
            ComponentActionEnum action) {
        assert ComponentActionEnum.BUILD.equals(action) || ComponentActionEnum.DEPLOY.equals(action);
        try {
            ComponentHandler componentHandler = get(ComponentHandler.class, "COMPONENT", componentType);
            if (ComponentActionEnum.BUILD.equals(action)) {
                return get(scriptClass, DynamicScriptKindEnum.COMPONENT_BUILD.toString(),
                        componentHandler.buildScriptName());
            } else {
                return get(scriptClass, DynamicScriptKindEnum.COMPONENT_DEPLOY.toString(),
                        componentHandler.deployScriptName());
            }
        } catch (Exception e) {
            log.error("failed to get executor via generic method, fallback|appId={}|componentType={}|" +
                            "componentName={}|action={}|exception={}", appId, componentType,
                    componentName, action, e.getMessage());
            if (ComponentTypeEnum.INTERNAL_ADDON.toString().equals(componentType)) {
                if ("tianji_productops".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_TIANJI_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_TIANJI_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("productops".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("authproxy".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_AUTHPROXY_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_AUTHPROXY_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("check".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_CHECK_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_CHECK_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("check_monitor".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_CHECK_MONITOR_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_CHECK_MONITOR_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("taskplatform".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_TASKPLATFORM_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_TASKPLATFORM_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("zmn".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_ZMN_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_ZMN_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("productopsv2".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_V2_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_V2_PRODUCTOPS_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if ("appmeta".equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_APP_META_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_APP_META_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else if (INTERNAL_ADDON_DEVELOPMENT_META.equals(componentName)) {
                    if (ComponentActionEnum.BUILD.equals(action)) {
                        return get(scriptClass,
                                DynamicScriptKindEnum.BUILD_IA_DEVELOPMENT_META_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    } else {
                        return get(scriptClass,
                                DynamicScriptKindEnum.DEPLOY_IA_DEVELOPMENT_META_COMPONENT.toString(),
                                DefaultConstant.DEFAULT_GROOVY_HANDLER);
                    }
                } else {
                    return null;
                }
            } else if (ComponentTypeEnum.RESOURCE_ADDON.toString().equals(componentType)) {
                if (ComponentActionEnum.BUILD.equals(action)) {
                    return get(scriptClass,
                            DynamicScriptKindEnum.BUILD_RESOURCE_ADDON_COMPONENT.toString(),
                            DefaultConstant.DEFAULT_GROOVY_HANDLER);
                } else {
                    return get(scriptClass,
                            DynamicScriptKindEnum.DEPLOY_RESOURCE_ADDON_COMPONENT.toString(),
                            DefaultConstant.DEFAULT_GROOVY_HANDLER);
                }
            } else {
                return null;
            }
        }
    }

    /**
     * 创建 Handler
     *
     * @param kind 动态脚本类型
     * @param name 动态脚本唯一标识
     * @return Handler
     */
    private GroovyHandler createOrUpdate(String kind, String name) {
        String key = keyGenerator(kind, name);
        DynamicScriptQueryCondition condition = DynamicScriptQueryCondition.builder()
                .kind(kind)
                .name(name)
                .withBlobs(true)
                .build();
        DynamicScriptDO script = dynamicScriptService.get(condition);
        if (script == null) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("cannot find handler|kind=%s|name=%s", kind, name));
        }

        try {
            String code = script.getCode();
            Class<?> clazz = groovyClassLoader.parseClass(code);
            Object handler = clazz.getDeclaredConstructor().newInstance();
            VERSION_MAP.put(key, script.getCurrentRevision());
            populateHandler(handler);
            HANDLER_INSTANCES.put(key, (GroovyHandler) handler);
            log.info("groovy handler has loaded|kind={}|name={}|revision={}",
                    kind, name, script.getCurrentRevision());
            return (GroovyHandler) handler;
        } catch (Exception e) {
            throw new AppException(AppErrorCode.USER_CONFIG_ERROR,
                    String.format("parse class from database failed in groovy loading progress|" +
                            "kind=%s|name=%s", kind, name), e);
        }
    }

    /**
     * 删除 Handler
     *
     * @param kind 动态脚本类型
     * @param name 动态脚本唯一标识
     */
    public void uninstall(String kind, String name) {
        String key = keyGenerator(kind, name);
        if (!VERSION_MAP.containsKey(key)) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS,
                    String.format("the plugin has not been loaded|kind=%s|name=%s", kind, name));
        }
        synchronized (GroovyHandlerFactory.class) {
            VERSION_MAP.remove(key);
            HANDLER_INSTANCES.remove(key);
            log.info("groovy handler has unloaded|kind={}|name={}", kind, name);
        }
    }

    /**
     * 依赖注入
     *
     * @param handler handler
     */
    @SuppressWarnings("GroovyAssignabilityCheck")
    private static void populateHandler(Object handler) throws IllegalAccessException {
        if (handler == null) {
            return;
        }

        Field[] fields = handler.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }

            Object fieldBean = null;
            if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && qualifier.value().length() > 0) {
                    fieldBean = SpringBeanUtil.getApplicationContext().getBean(qualifier.value());
                } else {
                    fieldBean = SpringBeanUtil.getApplicationContext().getBean(field.getType());
                }
            }

            if (fieldBean != null) {
                field.setAccessible(true);
                field.set(handler, fieldBean);
            }
        }
    }

    /**
     * 生成 Map 的 Key 字符串
     *
     * @param kind 动态脚本类型
     * @param name 动态脚本名称
     * @return Key String
     */
    private static String keyGenerator(String kind, String name) {
        return String.format("%s|%s", kind, name);
    }

    /**
     * 将 Key 还原回 Kind + Name 的形式
     *
     * @param key Map Key
     * @return Keytore 对象
     */
    private static KeyStore keySplit(String key) {
        String[] array = key.split("\\|");
        if (array.length != 2) {
            throw new AppException(AppErrorCode.INVALID_USER_ARGS, "invalid groovy handler key " + key);
        }
        return KeyStore.builder()
                .kind(array[0])
                .name(array[1])
                .build();
    }
}

/**
 * Key 存储
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
@Data
@Builder
class KeyStore {

    /**
     * 类型
     */
    private String kind;

    /**
     * 名称
     */
    private String name;
}