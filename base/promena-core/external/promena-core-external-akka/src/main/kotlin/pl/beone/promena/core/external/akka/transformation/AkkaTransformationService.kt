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
import org.slf4j.LoggerFactory
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationException
import pl.beone.promena.core.applicationmodel.exception.transformation.TransformationTerminationException
import pl.beone.promena.core.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.transformation.TransformationService
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.util.*
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.data.singleDataDescriptor
import pl.beone.promena.transformer.contract.data.toDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.Transformation
import java.time.Duration

private data class ActorTransformerDescriptor(val actorRef: ActorRef,
                                              val targetMediaType: MediaType,
                                              val parameters: Parameters)

class AkkaTransformationService(private val actorMaterializer: ActorMaterializer,
                                private val actorService: ActorService) : TransformationService {

    companion object {
        private val logger = LoggerFactory.getLogger(AkkaTransformationService::class.java)
    }

    override fun transform(transformation: Transformation, dataDescriptor: DataDescriptor): TransformedDataDescriptors {
        logBeforeTransformation(transformation, dataDescriptor)

        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            try {
                val actorTransformerDescriptors = getTransformerDescriptorsWithActorRef(transformation)

                unwrapExecutionException {
                    createSource(dataDescriptor)
                            .viaIntermediateTransformers(getIntermediateTransformers(actorTransformerDescriptors))
                            .viaFinalTransformer(getFinalTransformer(actorTransformerDescriptors))
                            .runWith(Sink.head(), actorMaterializer)
                            .toCompletableFuture()
                            .get()
                }
            } catch (e: Exception) {
                throw processException(transformation, dataDescriptor, e)
            }
        }

        logAfterTransformation(transformation, measuredTimeMs, transformedDataDescriptors)

        return transformedDataDescriptors
    }

    private fun logBeforeTransformation(transformation: Transformation, dataDescriptor: DataDescriptor) {
        if (logger.isDebugEnabled) {
            logger.debug("Transforming <{}> <{} source(s)>: [{}]...",
                         transformation,
                         dataDescriptor.descriptors.size,
                         dataDescriptor.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.mediaType}>" })
        } else {
            logger.info("Transforming <{}> <{} source(s)>: [{}]...",
                        transformation,
                        dataDescriptor.descriptors.size,
                        dataDescriptor.descriptors.joinToString(", ") { "<${it.mediaType}>" })
        }
    }

    private fun getTransformerDescriptorsWithActorRef(transformation: Transformation): List<ActorTransformerDescriptor> =
            transformation.transformers.map { (id, mediaType, parameters) ->
                ActorTransformerDescriptor(actorService.getTransformationActor(id), mediaType, parameters)
            }

    private fun createSource(dataDescriptor: DataDescriptor): Source<DataDescriptor, NotUsed> =
            Source.single(dataDescriptor)

    private fun getIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): List<ActorTransformerDescriptor> =
            actorTransformerDescriptors.dropLast(1)

    private fun Source<DataDescriptor, NotUsed>.viaIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): Source<DataDescriptor, NotUsed> =
            actorTransformerDescriptors.map { (actorRef, targetMediaType, parameters) ->
                createTransformerFlow(actorRef, targetMediaType, parameters)
                        .map { it.toSequentialDataDescriptors(targetMediaType) }
            }.applyFlows(this)

    private fun <T> List<Flow<T, T, NotUsed>>.applyFlows(source: Source<T, NotUsed>): Source<T, NotUsed> =
            fold(source, { acc, flow -> acc.via(flow) })

    private fun getFinalTransformer(actorTransformerDescriptors: List<ActorTransformerDescriptor>): ActorTransformerDescriptor =
            actorTransformerDescriptors.last()

    private fun Source<DataDescriptor, NotUsed>.viaFinalTransformer(actorTransformerDescriptor: ActorTransformerDescriptor): Source<TransformedDataDescriptors, NotUsed> {
        val (actorRef, targetMediaType, parameters) = actorTransformerDescriptor
        return via(createTransformerFlow(actorRef, targetMediaType, parameters))
    }

    private fun createTransformerFlow(actorRef: ActorRef,
                                      mediaType: MediaType,
                                      parameters: Parameters): Flow<DataDescriptor, TransformedDataDescriptors, NotUsed> =
            Flow.of(getClazz<DataDescriptor>())
                    .map { dataDescriptor -> ToTransformMessage(dataDescriptor, mediaType, parameters) }
                    .ask(actorRef, TransformedMessage::class.java, parameters.getTimeoutOrInfiniteIfNotFound().toTimeout())
                    .map { it.transformedDataDescriptors }

    private fun TransformedDataDescriptors.toSequentialDataDescriptors(mediaType: MediaType): DataDescriptor =
            descriptors.map { (data, metadata) -> singleDataDescriptor(data, mediaType, metadata) }
                    .toDataDescriptor()

    private fun Duration.toTimeout(): Timeout = Timeout.create(this)

    private fun logAfterTransformation(transformation: Transformation,
                                       measuredTimeMs: Long,
                                       transformedDataDescriptors: TransformedDataDescriptors) {
        if (logger.isDebugEnabled) {
            logger.debug("Finished transforming <{}> <{} result(s)> in <{} s>: [{}]",
                         transformation,
                         transformedDataDescriptors.descriptors.size,
                         measuredTimeMs.toSeconds(),
                         transformedDataDescriptors.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.metadata}>" })
        } else {
            logger.info("Finished transforming <{}> <{} result(s)> in <{} s>: [{}]",
                        transformation,
                        transformedDataDescriptors.descriptors.size,
                        measuredTimeMs.toSeconds(),
                        transformedDataDescriptors.descriptors.joinToString(", ") { "<${it.metadata}>" })
        }
    }

    private fun processException(transformation: Transformation, dataDescriptor: DataDescriptor, exception: Exception): Exception {
        val exceptionDescriptor = generateExceptionDescriptor(transformation, dataDescriptor)

        return when (exception) {
            is TransformerException            ->
                TransformationException("Couldn't perform the transformation | ${exception.message} | $exceptionDescriptor", exception)
            is AskTimeoutException             ->
                TransformationException("Couldn't perform the transformation because timeout has been reached | $exceptionDescriptor",
                                        exception)
            is AbruptStageTerminationException ->
                TransformationTerminationException("Couldn't transform because the transformation was abruptly terminated | $exceptionDescriptor",
                                                   exception)
            else                               ->
                TransformationException("Couldn't transform because a unknown error occurred. Check Promena logs for more details | $exceptionDescriptor",
                                        exception)
        }
    }

    private fun generateExceptionDescriptor(transformation: Transformation,
                                            dataDescriptor: DataDescriptor): String =
            "<:1> <:2 source(s)>: [:3]"
                    .replace(":1", transformation.toString())
                    .replace(":2", dataDescriptor.descriptors.size.toString())
                    .replace(":3", dataDescriptor.getLocationsInString())

    private fun DataDescriptor.getLocationsInString(): String =
            descriptors.joinToString(", ") {
                try {
                    "<${it.data.getLocation()}, ${it.mediaType}>"
                } catch (e: UnsupportedOperationException) {
                    "<no location, ${it.mediaType}>"
                }
            }
}