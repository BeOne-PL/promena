package pl.beone.promena.core.external.akka.transformation

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import akka.stream.AbruptStageTerminationException
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import mu.KotlinLogging
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.core.contract.actor.TransformerActorGetter
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.extension.toTimeout
import pl.beone.promena.core.external.akka.util.measureTimeMillisWithContent
import pl.beone.promena.core.external.akka.util.unwrapExecutionException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.toDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformer.TransformerId
import pl.beone.promena.transformer.internal.extension.format
import pl.beone.promena.transformer.internal.extension.toPrettyString
import pl.beone.promena.transformer.internal.extension.toSeconds
import pl.beone.promena.transformer.internal.extension.toSimplePrettyString
import java.time.Duration

private data class ActorTransformerDescriptor(
    val transformerId: TransformerId,
    val transformerActorRef: ActorRef,
    val targetMediaType: MediaType,
    val parameters: Parameters
)

class AkkaTransformationService(
    private val timeout: Duration,
    private val interruptionTimeoutDelay: Duration,
    private val actorMaterializer: ActorMaterializer,
    private val transformerActorGetter: TransformerActorGetter
) : TransformationService {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun transform(transformation: Transformation, dataDescriptor: DataDescriptor): TransformedDataDescriptor {
        logBeforeTransformation(transformation, dataDescriptor)

        val (transformedDataDescriptor, measuredTimeMs) = measureTimeMillisWithContent {
            try {
                val actorTransformerDescriptors = getActorTransformerDescriptors(transformation)

                unwrapExecutionException {
                    createSource(dataDescriptor)
                        .viaIntermediateTransformers(getIntermediateTransformers(actorTransformerDescriptors))
                        .viaFinalTransformer(getFinalTransformer(actorTransformerDescriptors))
                        .runWith(Sink.head(), actorMaterializer)
                        .toCompletableFuture()
                        .get()
                }
            } catch (e: Exception) {
                throw convertException(e)
            }
        }

        logAfterTransformation(transformation, dataDescriptor, transformedDataDescriptor, measuredTimeMs)

        return transformedDataDescriptor
    }

    private fun logBeforeTransformation(transformation: Transformation, dataDescriptor: DataDescriptor) {
        val message = if (logger.isDebugEnabled) {
            "Transforming\n" +
                    "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                    "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toPrettyString()}"
        } else {
            "Transforming\n" +
                    "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                    "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toSimplePrettyString()}"
        }

        logger.info { message }
    }

    private fun getActorTransformerDescriptors(transformation: Transformation): List<ActorTransformerDescriptor> =
        transformation.transformers.map { (id, mediaType, parameters) ->
            ActorTransformerDescriptor(id, transformerActorGetter.get(id), mediaType, parameters)
        }

    private fun createSource(dataDescriptor: DataDescriptor): Source<DataDescriptor, NotUsed> =
        Source.single(dataDescriptor)

    private fun getIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): List<ActorTransformerDescriptor> =
        actorTransformerDescriptors.dropLast(1)

    private fun Source<DataDescriptor, NotUsed>.viaIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): Source<DataDescriptor, NotUsed> =
        actorTransformerDescriptors.map { (transformerId, transformerActorRef, targetMediaType, parameters) ->
            createTransformerFlow(transformerId, transformerActorRef, targetMediaType, parameters)
                .map { it.toSequentialDataDescriptors(targetMediaType) }
        }.applyFlows(this)

    private fun <T> List<Flow<T, T, NotUsed>>.applyFlows(source: Source<T, NotUsed>): Source<T, NotUsed> =
        fold(source, { acc, flow -> acc.via(flow) })

    private fun getFinalTransformer(actorTransformerDescriptors: List<ActorTransformerDescriptor>): ActorTransformerDescriptor =
        actorTransformerDescriptors.last()

    private fun Source<DataDescriptor, NotUsed>.viaFinalTransformer(actorTransformerDescriptor: ActorTransformerDescriptor): Source<TransformedDataDescriptor, NotUsed> {
        val (transformerId, targetActorRef, targetMediaType, parameters) = actorTransformerDescriptor
        return via(createTransformerFlow(transformerId, targetActorRef, targetMediaType, parameters))
    }

    private fun createTransformerFlow(
        transformerId: TransformerId,
        transformerActorRef: ActorRef,
        mediaType: MediaType,
        parameters: Parameters
    ): Flow<DataDescriptor, TransformedDataDescriptor, NotUsed> =
        Flow.of(getClazz<DataDescriptor>())
            .map { dataDescriptor -> ToTransformMessage(transformerId, dataDescriptor, mediaType, parameters) }
            .ask(transformerActorRef, TransformedMessage::class.java, (parameters.getTimeoutOrDefault(timeout) + interruptionTimeoutDelay).toTimeout())
            .map { (transformedDataDescriptor) -> transformedDataDescriptor }

    private fun TransformedDataDescriptor.toSequentialDataDescriptors(mediaType: MediaType): DataDescriptor =
        descriptors.map { (data, metadata) -> singleDataDescriptor(data, mediaType, metadata) }
            .toDataDescriptor()

    private fun logAfterTransformation(
        transformation: Transformation,
        dataDescriptor: DataDescriptor,
        transformedDataDescriptor: TransformedDataDescriptor,
        measuredTimeMs: Long
    ) {
        val message = if (logger.isDebugEnabled) {
            "Transformed in <${measuredTimeMs.toSeconds().format(3)} s>\n" +
                    "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                    "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toPrettyString()}\n" +
                    "> Transformed data descriptor <${transformedDataDescriptor.descriptors.size}>: ${transformedDataDescriptor.toPrettyString()}"
        } else {
            "Transformed in <${measuredTimeMs.toSeconds().format(3)} s>\n" +
                    "> Transformation <${transformation.transformers.size}>: ${transformation.toPrettyString()}\n" +
                    "> Data descriptor <${dataDescriptor.descriptors.size}>: ${dataDescriptor.toSimplePrettyString()}\n" +
                    "> Transformed data descriptor <${transformedDataDescriptor.descriptors.size}>: ${transformedDataDescriptor.toSimplePrettyString()}"
        }

        logger.info { message }
    }

    private fun convertException(e: Exception): Exception =
        when (e) {
            is TransformerException ->
                TransformationException(e.message!!, e.javaClass.canonicalName)
            is TransformationNotSupportedException ->
                TransformationException("Transformation isn't supported | ${e.message}", e.javaClass.canonicalName)
            is AskTimeoutException ->
                TransformationTerminationException(
                    "Transformation timeout has been reached. It's highly likely that Promena performing transformation has been shutdown",
                    AskTimeoutException::class.java.canonicalName
                )
            is AbruptStageTerminationException ->
                TransformationTerminationException("Transformation has been abruptly terminated")
            else ->
                e
        }
}