package pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.actor.config

import akka.actor.ActorSystem
import akka.cluster.metrics.MetricsSelector
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.actorcreator.adaptiveloadbalancing.configuration.external.akka.actor.config.AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator

@Configuration
class AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreatorContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    fun adaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(
        environment: Environment,
        actorSystem: ActorSystem
    ): AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator {
        val metricsSelector = getSelectorInstance(environment)

        logger.info { "Adaptive load balancing metrics selector: <${metricsSelector::class.qualifiedName}>" }

        return AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator(actorSystem, metricsSelector)
    }

    private fun getSelectorInstance(environment: Environment): MetricsSelector {
        val property = environment.getRequiredProperty("actor-creator.adaptive-load-balancing.metrics-selector")
        return if (property.contains("::")) createUsingStaticMethod(property) else createUsingConstructor(property)
    }

    private fun createUsingStaticMethod(property: String): MetricsSelector {
        val (className, methodName) = property.split("::")

        return try {
            Class.forName(className)
                .methods
                .firstOrNull { it.name == methodName && it.parameterCount == 0 }
                ?.let { it.invoke(null) as MetricsSelector }
                ?: error("Class <$className> doesn't contain <$methodName> method")
        } catch (e: Exception) {
            throw IllegalStateException("Couldn't create MetricsSelector using <$property>. It must be static method without arguments!", e)
        }
    }

    private fun createUsingConstructor(property: String): MetricsSelector =
        try {
            Class.forName(property)
                .getDeclaredConstructor()
                .newInstance() as MetricsSelector
        } catch (e: Exception) {
            throw IllegalStateException("Couldn't create MetricsSelector using <$property>. It must be class with constructor without arguments!", e)
        }

}