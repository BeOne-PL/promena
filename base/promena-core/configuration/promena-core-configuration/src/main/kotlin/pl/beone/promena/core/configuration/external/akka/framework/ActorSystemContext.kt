package pl.beone.promena.core.configuration.external.akka.framework

import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment

@Configuration
class ActorSystemContext {

    companion object {
        private val logger = LoggerFactory.getLogger(ActorSystemContext::class.java)
    }

    @Bean
    fun actorSystem(environment: Environment): ActorSystem {
        setAkkaProperties(getAkkaProperties(environment))

        return ActorSystem.create("TransformationServer", ConfigFactory.load("kryo-mapping.conf"))
    }

    private fun getAkkaProperties(environment: Environment): List<Pair<String, String>> =
            (environment as StandardEnvironment).propertySources
                    .filter { it is MapPropertySource }
                    .map { it as MapPropertySource }
                    .flatMap { it.source.keys }
                    .filter { key -> key.startsWith("akka.") }
                    .map { key -> key to environment.getRequiredProperty(key) }

    private fun setAkkaProperties(akkaProperties: List<Pair<String, String>>) {
        logger.info("Found <{}> AKKA property(ies)", akkaProperties.size)
        akkaProperties.forEach { (key, value) ->
            System.setProperty(key, value)
            logger.debug("> Set <{}={}>", key, value)
        }
    }
}
