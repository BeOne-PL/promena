package pl.beone.promena.core.configuration.external.akka.framework

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

@Configuration
class ActorSystemContext {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @Bean
    @ConditionalOnMissingBean(ActorSystem::class)
    fun actorSystem(environment: Environment): ActorSystem {
        setAkkaProperties(getAkkaProperties(environment))

        return ActorSystem.create("Promena", ConfigFactory.load("kryo-mapping.conf"))
    }

    private fun getAkkaProperties(environment: Environment): List<Pair<String, String>> =
        (environment as StandardEnvironment).propertySources
            .filterIsInstance<MapPropertySource>()
            .flatMap { it.source.keys }
            .filter { key -> key.startsWith("akka.") }
            .map { key -> key to environment.getRequiredProperty(key) }

    private fun setAkkaProperties(akkaProperties: List<Pair<String, String>>) {
        logger.info { "Found <${akkaProperties.size}> AKKA property(ies)" }
        akkaProperties.forEach { (key, value) ->
            System.setProperty(key, value)
            logger.debug { "> Set <$key=$value>" }
        }
    }
}
