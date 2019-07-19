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
import pl.beone.promena.transformer.contract.data.DataDescriptors
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptors
import pl.beone.promena.transformer.contract.data.dataDescriptor
import pl.beone.promena.transformer.contract.data.dataDescriptors
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformation.TransformationFlow
import java.time.Duration

private data class ActorTransformerDescriptor(val actorRef: ActorRef,
                                              val targetMediaType: MediaType,
                                              val parameters: Parameters)

class AkkaTransformationService(private val actorMaterializer: ActorMaterializer,
                                private val actorService: ActorService) : TransformationService {

    companion object {
        private val logger = LoggerFactory.getLogger(AkkaTransformationService::class.java)
    }

    override fun transform(transformationFlow: TransformationFlow, dataDescriptors: DataDescriptors): TransformedDataDescriptors {
        logBeforeTransformation(transformationFlow, dataDescriptors)

        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            try {
                val actorTransformerDescriptors = getTransformerDescriptorsWithActorRef(transformationFlow)

                unwrapExecutionException {
                    createSource(dataDescriptors)
                            .viaIntermediateTransformers(getIntermediateTransformers(actorTransformerDescriptors))
                            .viaFinalTransformer(getFinalTransformer(actorTransformerDescriptors))
                            .runWith(Sink.head(), actorMaterializer)
                            .toCompletableFuture()
                            .get()
                }
            } catch (e: Exception) {
                throw processException(transformationFlow, dataDescriptors, e)
            }
        }

        logAfterTransformation(transformationFlow, measuredTimeMs, transformedDataDescriptors)

        return transformedDataDescriptors
    }

    private fun logBeforeTransformation(transformationFlow: TransformationFlow, dataDescriptors: DataDescriptors) {
        if (logger.isDebugEnabled) {
            logger.debug("Transforming <{}> <{} source(s)>: [{}]...",
                         transformationFlow,
                         dataDescriptors.descriptors.size,
                         dataDescriptors.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.mediaType}>" })
        } else {
            logger.info("Transforming <{}> <{} source(s)>: [{}]...",
                        transformationFlow,
                        dataDescriptors.descriptors.size,
                        dataDescriptors.descriptors.joinToString(", ") { "<${it.mediaType}>" })
        }
    }

    private fun getTransformerDescriptorsWithActorRef(transformationFlow: TransformationFlow): List<ActorTransformerDescriptor> =
            transformationFlow.transformers.map { (id, mediaType, parameters) ->
                ActorTransformerDescriptor(actorService.getTransformationActor(id), mediaType, parameters)
            }

    private fun createSource(dataDescriptors: DataDescriptors): Source<DataDescriptors, NotUsed> =
            Source.single(dataDescriptors)

    private fun getIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): List<ActorTransformerDescriptor> =
            actorTransformerDescriptors.dropLast(1)

    private fun Source<DataDescriptors, NotUsed>.viaIntermediateTransformers(actorTransformerDescriptors: List<ActorTransformerDescriptor>): Source<DataDescriptors, NotUsed> =
            actorTransformerDescriptors.map { (actorRef, targetMediaType, parameters) ->
                createTransformerFlow(actorRef, targetMediaType, parameters)
                        .map { it.toSequentialDataDescriptors(targetMediaType) }
            }.applyFlows(this)

    private fun <T> List<Flow<T, T, NotUsed>>.applyFlows(source: Source<T, NotUsed>): Source<T, NotUsed> =
            fold(source, { acc, flow -> acc.via(flow) })

    private fun getFinalTransformer(actorTransformerDescriptors: List<ActorTransformerDescriptor>): ActorTransformerDescriptor =
            actorTransformerDescriptors.last()

    private fun Source<DataDescriptors, NotUsed>.viaFinalTransformer(actorTransformerDescriptor: ActorTransformerDescriptor): Source<TransformedDataDescriptors, NotUsed> {
        val (actorRef, targetMediaType, parameters) = actorTransformerDescriptor
        return via(createTransformerFlow(actorRef, targetMediaType, parameters))
    }

    private fun createTransformerFlow(actorRef: ActorRef,
                                      mediaType: MediaType,
                                      parameters: Parameters): Flow<DataDescriptors, TransformedDataDescriptors, NotUsed> =
            Flow.of(getClazz<DataDescriptors>())
                    .map { dataDescriptors -> ToTransformMessage(dataDescriptors, mediaType, parameters) }
                    .ask(actorRef, TransformedMessage::class.java, parameters.getTimeoutOrInfiniteIfNotFound().toTimeout())
                    .map { it.transformedDataDescriptors }

    private fun TransformedDataDescriptors.toSequentialDataDescriptors(mediaType: MediaType): DataDescriptors =
            dataDescriptors(
                    descriptors.map { (data, metadata) -> dataDescriptor(data, mediaType, metadata) }
            )

    private fun Duration.toTimeout(): Timeout = Timeout.create(this)

    private fun logAfterTransformation(transformationFlow: TransformationFlow,
                                       measuredTimeMs: Long,
                                       transformedDataDescriptors: TransformedDataDescriptors) {
        if (logger.isDebugEnabled) {
            logger.debug("Finished transforming <{}> <{} result(s)> in <{} s>: [{}]",
                         transformationFlow,
                         transformedDataDescriptors.descriptors.size,
                         measuredTimeMs.toSeconds(),
                         transformedDataDescriptors.descriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.metadata}>" })
        } else {
            logger.info("Finished transforming <{}> <{} result(s)> in <{} s>: [{}]",
                        transformationFlow,
                        transformedDataDescriptors.descriptors.size,
                        measuredTimeMs.toSeconds(),
                        transformedDataDescriptors.descriptors.joinToString(", ") { "<${it.metadata}>" })
        }
    }

    private fun processException(transformationFlow: TransformationFlow, dataDescriptors: DataDescriptors, exception: Exception): Exception {
        val exceptionDescriptor = generateExceptionDescriptor(transformationFlow, dataDescriptors)

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

    private fun generateExceptionDescriptor(transformationFlow: TransformationFlow,
                                            dataDescriptors: DataDescriptors): String =
            "<:1> <:2 source(s)>: [:3]"
                    .replace(":1", transformationFlow.toString())
                    .replace(":2", dataDescriptors.descriptors.size.toString())
                    .replace(":3", dataDescriptors.getLocationsInString())

    private fun DataDescriptors.getLocationsInString(): String =
            descriptors.joinToString(", ") {
                try {
                    "<${it.data.getLocation()}, ${it.mediaType}>"
                } catch (e: UnsupportedOperationException) {
                    "<no location, ${it.mediaType}>"
                }
            }
}