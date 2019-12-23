package pl.beone.promena.core.configuration.external.akka.transformation

import akka.stream.ActorMaterializer
import org.joda.time.format.PeriodFormatterBuilder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import pl.beone.promena.core.contract.actor.TransformerActorGetter
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.transformation.AkkaTransformationService
import java.time.Duration

@Configuration
class AkkaTransformerServiceContext {

    @Bean
    @ConditionalOnMissingBean(TransformationService::class)
    fun akkaTransformationService(
        environment: Environment,
        actorMaterializer: ActorMaterializer,
        transformerActorGetter: TransformerActorGetter
    ) =
        AkkaTransformationService(
            environment.getRequiredProperty("core.transformation.timeout").toDuration(),
            environment.getRequiredProperty("core.transformation.interruption-timeout-delay").toDuration(),
            actorMaterializer,
            transformerActorGetter
        )

    private fun String.toDuration(): Duration {
        val formatter = PeriodFormatterBuilder()
            .appendDays().appendSuffix("d")
            .appendHours().appendSuffix("h")
            .appendMinutes().appendSuffix("m")
            .appendSeconds().appendSuffix("s")
            .appendMillis().appendSuffix("ms")
            .toFormatter()

        return Duration.ofMillis(formatter.parsePeriod(this).toStandardDuration().millis)
    }
}