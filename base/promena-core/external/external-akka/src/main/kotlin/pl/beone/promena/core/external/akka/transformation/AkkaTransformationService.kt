package pl.beone.promena.core.external.akka.transformation

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import akka.stream.AbruptStageTerminationException
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.util.Timeout
import mu.KotlinLogging
import pl.beone.lib.typeconverter.internal.getClazz
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.core.contract.actor.TransformerActorGetter
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.extension.format
import pl.beone.promena.core.external.akka.extension.getTimeoutOrInfiniteIfNotFound
import pl.beone.promena.core.external.akka.extension.toMB
import pl.beone.promena.core.external.akka.extension.toSeconds
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
import java.time.Duration

private data class ActorTransformerDescriptor(
    val transformerId: TransformerId,
    val transformerActorRef: ActorRef,
    val targetMediaType: MediaType,
    val parameters: Parameters
)

class AkkaTransformationService(
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
                throw convertException(transformation, e)
            }
        }

        logAfterTransformation(transformation, measuredTimeMs, transformedDataDescriptor)

        return transformedDataDescriptor
    }

    private fun logBeforeTransformation(transformation: Transformation, dataDescriptor: DataDescriptor) {
        val message = if (logger.isDebugEnabled) {
            "Transforming <$transformation> <${dataDescriptor.descriptors.size} source(s)>: " +
                    "[${dataDescriptor.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.mediaType}, ${it.metadata}>" }}]..."
        } else {
            "Transforming <$transformation> <${dataDescriptor.descriptors.size} source(s)>: " +
                    "[${dataDescriptor.descriptors.joinToString(", ") { "<${it.mediaType}, ${it.metadata}>" }}]..."
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
            .ask(transformerActorRef, TransformedMessage::class.java, parameters.getTimeoutOrInfiniteIfNotFound(interruptionTimeoutDelay).toTimeout())
            .map { (transformedDataDescriptor) -> transformedDataDescriptor }

    private fun TransformedDataDescriptor.toSequentialDataDescriptors(mediaType: MediaType): DataDescriptor =
        descriptors.map { (data, metadata) -> singleDataDescriptor(data, mediaType, metadata) }
            .toDataDescriptor()

    private fun Duration.toTimeout(): Timeout =
        Timeout.create(this)

    private fun logAfterTransformation(
        transformation: Transformation,
        measuredTimeMs: Long,
        transformedDataDescriptor: TransformedDataDescriptor
    ) {
        val message = if (logger.isDebugEnabled) {
            "Finished transforming <$transformation> <${transformedDataDescriptor.descriptors.size} result(s)> in <${measuredTimeMs.toSeconds()} s>: " +
                    "[${transformedDataDescriptor.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.metadata}>" }}]"

        } else {
            "Finished transforming <$transformation> <${transformedDataDescriptor.descriptors.size} result(s)> in <${measuredTimeMs.toSeconds()} s>: " +
                    "[${transformedDataDescriptor.descriptors.joinToString(", ") { "<${it.metadata}>" }}]"

        }

        logger.info { message }
    }

    private fun convertException(transformation: Transformation, exception: Exception): Exception =
        when (exception) {
            is TransformerException ->
                TransformationException(transformation, "Couldn't transform | ${exception.message}", exception)
            is TransformationNotSupportedException ->
                TransformationException(transformation, "Couldn't transform because given transformation isn't supported | ${exception.message}", exception)
            is AskTimeoutException ->
                TransformationException(transformation, "Couldn't transform because given timeout has been reached", exception)
            is AbruptStageTerminationException ->
                TransformationTerminationException(transformation, "Couldn't transform because it was abruptly terminated", exception)
            else ->
                exception
        }
}