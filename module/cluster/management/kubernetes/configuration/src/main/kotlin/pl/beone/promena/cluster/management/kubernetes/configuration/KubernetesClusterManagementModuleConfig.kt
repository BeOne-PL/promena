package pl.beone.promena.cluster.management.kubernetes.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:module-cluster-management-kubernetes.properties")
class KubernetesClusterManagementModuleConfig