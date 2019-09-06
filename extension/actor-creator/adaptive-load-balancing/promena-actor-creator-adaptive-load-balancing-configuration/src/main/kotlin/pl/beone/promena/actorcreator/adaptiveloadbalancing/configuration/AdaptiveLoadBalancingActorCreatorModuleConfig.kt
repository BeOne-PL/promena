package pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource("classpath:extension-actor-creator-adaptive-load-balancing.properties")
class AdaptiveLoadBalancingActorCreatorModuleConfig