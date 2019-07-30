package pl.beone.promena.core.external.akka.transformer.config

import akka.actor.ActorRef
import akka.actor.Props
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.core.external.akka.actor.transformer.GroupedByNameTransformerActor
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.applicationmodel.exception.DuplicatedTransformerIdException
import pl.beone.promena.transformer.contract.Transformer

class GroupedByNameTransformersCreator(
    private val transformerConfig: TransformerConfig,
    private val internalCommunicationConverter: InternalCommunicationConverter,
    private val actorCreator: ActorCreator
) : TransformersCreator {

    companion object {
        private val logger = LoggerFactory.getLogger(GroupedByNameTransformersCreator::class.java)
    }

    override fun create(transformers: List<Transformer>): List<TransformerActorDescriptor> {
        logger.info("Found <${transformers.size}> transformer(s). Actor config: ${actorCreator::class.java.canonicalName}")

        validateTransformers(transformers)

        return transformers.groupBy { transformerConfig.getTransformerId(it).name }
            .flatMap { (transformerId, transformers) ->
                val actors = transformers.getMaxActors()

                val transformerDescriptors = transformers
                    .map(::createTransformerDescriptor)

                val transformerActor = transformerDescriptors
                    .sortedBy { transformerDescriptor -> transformerDescriptor.transformer.getPriority() }
                    .let { transformerDescriptor -> createTransformerActor(transformerId, transformerDescriptor, actors) }

                logSuccessfulActorCreation(transformerId, transformers)

                transformerDescriptors.map { TransformerActorDescriptor(it.transformerId, transformerActor, actors) }
            }
    }

    private fun validateTransformers(transformers: List<Transformer>) {
        val notUniqueTransforms = transformers.groupBy { transformerConfig.getTransformerId(it) }
            .filter { (_, transformers) -> transformers.size >= 2 }
            .toList()

        if (notUniqueTransforms.isNotEmpty()) {
            throw DuplicatedTransformerIdException(
                "Detected <${notUniqueTransforms.size}> transformers with duplicated id:\n" +
                        notUniqueTransforms.joinToString("\n") { (transformerId, transformers) ->
                            "> $transformerId: <${transformers.joinToString(", ") { it::class.java.canonicalName }}>"
                        }
            )
        }
    }

    private fun List<Transformer>.getMaxActors(): Int =
        map { transformerConfig.getActors(it) }
            .max()!!

    private fun createTransformerDescriptor(transformer: Transformer): TransformerDescriptor =
        TransformerDescriptor(transformerConfig.getTransformerId(transformer), transformer)

    private fun Transformer.getPriority(): Int =
        transformerConfig.getPriority(this)

    private fun createTransformerActor(transformerId: String, transformerDescriptors: List<TransformerDescriptor>, maxActors: Int): ActorRef =
        actorCreator.create(
            transformerId,
            Props.create(GroupedByNameTransformerActor::class.java) {
                GroupedByNameTransformerActor(transformerId, transformerDescriptors, internalCommunicationConverter)
            },
            maxActors
        )

    private fun logSuccessfulActorCreation(transformerId: String, transformers: List<Transformer>) {
        logger.info(
            "> Registered <$transformerId> with <${transformers.size}> transformer(s) " +
                    "${transformers.map { "${it::class.java.canonicalName}, ${it.getPriority()} priority" }} and <${transformers.getMaxActors()}> actor(s) "
        )
    }

}