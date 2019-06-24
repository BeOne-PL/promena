package pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.actor.config

import akka.actor.ActorSystem
import akka.cluster.metrics.MetricsSelector
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.external.akka.actor.config.AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator

@Configuration
class AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreatorContext {

    companion object {
        private val logger = LoggerFactory.getLogger(AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreatorContext::class.java)
    }

    @Bean
    fun adaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(environment: Environment,
                                                                    actorSystem: ActorSystem): AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator {
        val metricsSelector = environment.getSelectorInstance()

        logger.info("Adaptive load balancing metrics selector: {}", metricsSelector::class.qualifiedName)

        return AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(actorSystem,
                                                                           metricsSelector)
                                                                    }

    private fun Environment.getSelectorInstance(): MetricsSelector {
        val property = this.getRequiredProperty("actor-creator.adaptive-load-balancing.metrics-selector")

        return if (property.contains("::")) {
            val (className, methodName) = property.split("::")

            try {
                Class.forName(className)
                        .methods
                        .firstOrNull { it.name == methodName }!!.let { it.invoke(null) as MetricsSelector }
            } catch (e: Exception) {
                throw Exception("Couldn't create MetricsSelector using <$property>. It must be static method without arguments!", e)
            }
        } else {
            try {
                Class.forName(property).getDeclaredConstructor().newInstance() as MetricsSelector
            } catch (e: Exception) {
                throw Exception("Couldn't create MetricsSelector using <$property>. It must be class with constructor without arguments!", e)
            }
        }
    }

}