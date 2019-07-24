package pl.beone.promena.core.external.akka.transformer.config

import akka.actor.Props
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithTransformerId
import pl.beone.promena.core.contract.actor.config.ActorCreator
import pl.beone.promena.core.contract.communication.internal.InternalCommunicationConverter
import pl.beone.promena.core.contract.transformer.config.TransformerConfig
import pl.beone.promena.core.contract.transformer.config.TransformersCreator
import pl.beone.promena.core.external.akka.actor.transformer.GroupedByNameTransformerActor
import pl.beone.promena.transformer.contract.Transformer

private data class TransformerWithPriority(val transformer: Transformer,
                                           val priority: Int)

class AkkaGroupedByNameTransformersCreator(private val transformerConfig: TransformerConfig,
                                           private val internalCommunicationConverter: InternalCommunicationConverter,
                                           private val actorCreator: ActorCreator) : TransformersCreator {

    companion object {
        private val logger = LoggerFactory.getLogger(AkkaGroupedByNameTransformersCreator::class.java)
    }

    override fun create(transformers: List<Transformer>): List<ActorRefWithTransformerId> {
        logger.info("Found <${transformers.size}> transformer(s). Actor config: ${actorCreator::class.simpleName}")

        return transformers.groupBy { transformerConfig.getTransformerId(it).name }
                .map { (transformerId, transformers) ->
                    val maxActors = getMaxActorsNumber(transformers)

                    val transformerWithPrioritySortedByPriorityList = sortTransformersByPriority(transformers)

                    val actorRefWithId = createTransformerActor(transformerId, transformerWithPrioritySortedByPriorityList, maxActors)

                    logger.info("> Registered <$transformerId> with <${transformers.size}> transformer(s) " +
                                "${transformerWithPrioritySortedByPriorityList.map { "${it.transformer::class.simpleName}, ${it.priority} priority" }}" +
                                " and <$maxActors> actor(s) ")

                    actorRefWithId
                }
    }

    private fun getMaxActorsNumber(transformers: List<Transformer>): Int =
        transformers.map { transformerConfig.getActors(it) }
                .max()!!

    private fun sortTransformersByPriority(transformers: List<Transformer>): List<TransformerWithPriority> =
        transformers.map { TransformerWithPriority(it, transformerConfig.getPriority(it)) }
                .sortedBy { it.priority }

    private fun createTransformerActor(transformerId: String,
                                       transformers: List<TransformerWithPriority>,
                                       maxActors: Int): ActorRefWithTransformerId =
        actorCreator.create(transformerId,
                            Props.create(GroupedByNameTransformerActor::class.java) {
                                GroupedByNameTransformerActor(transformerId, transformers.map { it.transformer }, internalCommunicationConverter)
                            },
                            maxActors)

}