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
import pl.beone.promena.transformer.contract.Transformer

class GroupedByNameTransformersCreator(private val transformerConfig: TransformerConfig,
                                       private val internalCommunicationConverter: InternalCommunicationConverter,
                                       private val actorCreator: ActorCreator) : TransformersCreator {

    companion object {
        private val logger = LoggerFactory.getLogger(GroupedByNameTransformersCreator::class.java)
    }

    override fun create(transformers: List<Transformer>): List<TransformerActorDescriptor> {
        logger.info("Found <${transformers.size}> transformer(s). Actor config: ${actorCreator::class.simpleName}")

        return transformers.groupBy { transformerConfig.getTransformerId(it).name }
                .flatMap { (transformerId, transformers) ->
                    val transformerDescriptors = transformers
                            .map(::createTransformerDescriptor)

                    val transformerActor = transformerDescriptors
                            .sortedBy { transformerDescriptor -> transformerDescriptor.transformer.getPriority() }
                            .let { transformerDescriptor -> createTransformerActor(transformerId, transformerDescriptor, transformers.getMaxActors()) }
                    
                    logSuccessfulActorCreation(transformerId, transformers)

                    transformerDescriptors.map { TransformerActorDescriptor(it.transformerId, transformerActor) }
                }
    }

    private fun List<Transformer>.getMaxActors(): Int =
        map { transformerConfig.getActors(it) }
                .max()!!

    private fun createTransformerDescriptor(transformer: Transformer): TransformerDescriptor =
        TransformerDescriptor(transformerConfig.getTransformerId(transformer), transformer)

    private fun Transformer.getPriority(): Int =
        transformerConfig.getPriority(this)

    private fun createTransformerActor(transformerId: String,
                                       transformerDescriptors: List<TransformerDescriptor>,
                                       maxActors: Int): ActorRef =
        actorCreator.create(transformerId,
                            Props.create(GroupedByNameTransformerActor::class.java) {
                                GroupedByNameTransformerActor(transformerId, transformerDescriptors, internalCommunicationConverter)
                            },
                            maxActors)

    private fun logSuccessfulActorCreation(transformerId: String, transformers: List<Transformer>) {
        logger.info("> Registered <$transformerId> with <${transformers.size}> transformer(s) " +
                    "${transformers.map { "${it::class.simpleName}, ${it.getPriority()} priority" }} and <${transformers.getMaxActors()}> actor(s) ")
    }

}