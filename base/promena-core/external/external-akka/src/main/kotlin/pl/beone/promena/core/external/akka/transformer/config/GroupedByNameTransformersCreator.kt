package pl.beone.promena.core.external.akka.transformer.config

import akka.actor.ActorRef
import akka.actor.Props
import mu.KotlinLogging
import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationCleaner
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.core.external.akka.actor.transformer.GroupedByNameTransformerActor
import pl.beone.promena.core.external.akka.applicationmodel.TransformerDescriptor
import pl.beone.promena.core.external.akka.extension.toCorrectActorName
import pl.beone.promena.transformer.contract.Transformer

class GroupedByNameTransformersCreator(
    private val transformerConfig: TransformerConfig,
    private val internalCommunicationConverter: InternalCommunicationConverter,
    private val internalCommunicationCleaner: InternalCommunicationCleaner,
    private val actorCreator: ActorCreator
) : TransformersCreator {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun create(transformers: List<Transformer>): List<TransformerActorDescriptor> {
        logger.info { "Found <${transformers.size}> transformer(s). Actor config: ${actorCreator::class.java.canonicalName}" }

        validateNumberOfTransformers(transformers)
        validateUniqueTransformers(transformers)

        return transformers.groupBy { transformerConfig.getTransformerId(it).name }
            .flatMap { (transformerName, transformers) ->
                val actors = transformers.getMaxActors()

                validateUniquePriorities(transformerName, transformers)

                val transformerDescriptors = transformers
                    .map(::createTransformerDescriptor)

                val transformerActor = transformerDescriptors
                    .sortedBy { transformerDescriptor -> transformerDescriptor.transformer.getPriority() }
                    .let { transformerDescriptor -> createTransformerActor(transformerName, transformerDescriptor, actors) }

                logSuccessfulActorCreation(transformerName, transformers)

                transformerDescriptors.map { TransformerActorDescriptor(it.transformerId, transformerActor, actors) }
            }
    }

    private fun validateNumberOfTransformers(transformers: List<Transformer>) {
        check(transformers.isNotEmpty()) {
            "No transformer was found. You must add at least <1> transformer"
        }
    }

    private fun validateUniqueTransformers(transformers: List<Transformer>) {
        val notUniqueTransformers = transformers.groupBy { transformerConfig.getTransformerId(it) }
            .filter { (_, transformers) -> transformers.size >= 2 }
            .toList()

        check(notUniqueTransformers.isEmpty()) {
            "Detected <${notUniqueTransformers.size}> transformers with duplicated id:\n" +
                    notUniqueTransformers.joinToString("\n") { (transformerId, transformers) ->
                        "> (${transformerId.name}, ${transformerId.subName}): ${transformers.joinToString(", ") { it::class.java.canonicalName }}"
                    }
        }
    }

    private fun validateUniquePriorities(transformerName: String, transformers: List<Transformer>) {
        val notUniqueTransformers = transformers.groupBy { transformerConfig.getPriority(it) }
            .filter { (_, transformers) -> transformers.size >= 2 }
            .toList()

        check(notUniqueTransformers.isEmpty()) {
            "Detected <${notUniqueTransformers.size}> transformers with duplicated priority:\n" +
                    notUniqueTransformers.joinToString("\n") { (priority, transformers) ->
                        "> ($transformerName) [priority: $priority]: ${transformers.joinToString(", ") { "${it::class.java.canonicalName}($transformerName, ${it.getSubName()})" }}"
                    }
        }
    }

    private fun List<Transformer>.getMaxActors(): Int =
        map(transformerConfig::getActors)
            .max()!!

    private fun createTransformerDescriptor(transformer: Transformer): TransformerDescriptor =
        TransformerDescriptor(transformerConfig.getTransformerId(transformer), transformer)

    private fun Transformer.getPriority(): Int =
        transformerConfig.getPriority(this)

    private fun createTransformerActor(transformerName: String, transformerDescriptors: List<TransformerDescriptor>, maxActors: Int): ActorRef =
        actorCreator.create(
            transformerName.toCorrectActorName(),
            Props.create(GroupedByNameTransformerActor::class.java) {
                GroupedByNameTransformerActor(transformerName, transformerDescriptors, internalCommunicationConverter, internalCommunicationCleaner)
            },
            maxActors
        )

    private fun logSuccessfulActorCreation(transformerName: String, transformers: List<Transformer>) {
        logger.info {
            "> Registered <$transformerName> with <${transformers.size}> transformer(s) " +
                    "${transformers.map { "<${it::class.java.canonicalName}(${it.getSubName()}), ${it.getPriority()} priority>" }} and <${transformers.getMaxActors()}> actor(s) "
        }
    }

    private fun Transformer.getSubName(): String =
        transformerConfig.getTransformerId(this).subName!!
}