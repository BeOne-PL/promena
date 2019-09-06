package pl.beone.promena.cluster.management.kubernetes.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:extension-cluster-management-kubernetes.properties")
class KubernetesClusterManagementModuleConfig