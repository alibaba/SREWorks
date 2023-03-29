package io.sreworks.plugin;

import io.fabric8.kubernetes.client.DefaultKubernetesClient;

/**
 * Kuberentes 相关插件 Provider
 *
 * @author yaoxing.gyx@alibaba-inc.com
 */
public interface KubernetesPluginProvider {

    /**
     * 根据 clusterId 获取 Kubernetes Client
     *
     * @param clusterId 集群 ID
     * @return Kubernetes Client
     */
    DefaultKubernetesClient get(String clusterId);
}
