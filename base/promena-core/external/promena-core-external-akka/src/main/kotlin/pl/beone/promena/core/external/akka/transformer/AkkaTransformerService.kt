package pl.beone.promena.core.external.akka.transformer

import akka.NotUsed
import akka.actor.ActorRef
import akka.pattern.AskTimeoutException
import akka.stream.ActorMaterializer
import akka.stream.javadsl.Flow
import akka.stream.javadsl.Sink
import akka.stream.javadsl.Source
import akka.util.Timeout
import org.slf4j.LoggerFactory
import pl.beone.promena.core.contract.actor.ActorService
import pl.beone.promena.core.contract.transformer.TransformerService
import pl.beone.promena.core.external.akka.actor.transformer.message.ToTransformMessage
import pl.beone.promena.core.external.akka.actor.transformer.message.TransformedMessage
import pl.beone.promena.core.external.akka.util.*
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import java.time.Duration

class AkkaTransformerService(private val actorMaterializer: ActorMaterializer,
                             private val actorService: ActorService) : TransformerService {

    companion object {
        private val logger = LoggerFactory.getLogger(AkkaTransformerService::class.java)
    }

    override fun transform(transformerId: String,
                           dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> {
        logBeforeTransformation(transformerId, dataDescriptors, targetMediaType, parameters)

        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            try {
                unwrapExecutionException {
                    createSource(dataDescriptors)
                            .via(createFlow(actorService.getTransformationActor(transformerId), targetMediaType, parameters))
                            .runWith(Sink.head(), actorMaterializer)
                            .toCompletableFuture()
                            .get()
                }
            } catch (e: Exception) {
                val exceptionDescriptor = generateExceptionDescriptor(transformerId, targetMediaType, parameters, dataDescriptors)

                when (e) {
                    is AskTimeoutException, is TransformerTimeoutException ->
                        throw TransformerTimeoutException("Couldn't transform because transformation time <${parameters.getTimeoutOrInfiniteIfNotFound()}> has expired | $exceptionDescriptor",
                                                          e)
                    is TransformerNotFoundException                        ->
                        throw TransformerNotFoundException("Couldn't transform because there is no suitable transformer | $exceptionDescriptor", e)
                    else                                                   ->
                        throw TransformerException("Couldn't transform because an error occurred | $exceptionDescriptor", e)
                }
            }
        }

        logAfterTransformation(transformerId, targetMediaType, parameters, measuredTimeMs, transformedDataDescriptors)

        return transformedDataDescriptors
    }

    private fun createSource(dataDescriptors: List<DataDescriptor>): Source<List<DataDescriptor>, NotUsed> =
            Source.single(dataDescriptors)

    private fun logBeforeTransformation(transformerId: String,
                                        dataDescriptors: List<DataDescriptor>,
                                        targetMediaType: MediaType,
                                        parameters: Parameters) {
        if (logger.isDebugEnabled) {
            logger.debug("Transforming <{}> <{}, {}> <{} source(s)>: [{}]...",
                         transformerId,
                         targetMediaType,
                         parameters,
                         dataDescriptors.size,
                         dataDescriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.mediaType}>" })
        } else {
            logger.info("Transforming <{}> <{}, {}> <{} source(s)>: [{}]...",
                        transformerId,
                        targetMediaType,
                        parameters,
                        dataDescriptors.size,
                        dataDescriptors.joinToString(", ") { "<${it.mediaType}>" })
        }
    }

    private fun createFlow(actorSelection: ActorRef,
                           targetMediaType: MediaType,
                           parameters: Parameters): Flow<List<DataDescriptor>, List<TransformedDataDescriptor>, NotUsed> =
            Flow.of(getClazz<List<DataDescriptor>>())
                    .map { ToTransformMessage(it, targetMediaType, parameters) }
                    .ask(actorSelection, TransformedMessage::class.java, parameters.getTimeoutOrInfiniteIfNotFound().toTimeout())
                    .map { it.transformedDataDescriptors }

    private fun Parameters.getTimeoutOrInfiniteIfNotFound(): Long =
            try {
                this.getTimeout()
            } catch (e: Exception) {
                infinite
            }

    private fun Long.toTimeout(): Timeout = Timeout.create(Duration.ofMillis(this))

    private fun logAfterTransformation(transformerId: String,
                                       targetMediaType: MediaType,
                                       parameters: Parameters,
                                       measuredTimeMs: Long,
                                       transformedDataDescriptors: List<TransformedDataDescriptor>) {
        if (logger.isDebugEnabled) {
            logger.debug("Finished transforming <{}> <{}, {}> <{} result(s)> in <{} s>: [{}]",
                         transformerId,
                         targetMediaType,
                         parameters,
                         transformedDataDescriptors.size,
                         measuredTimeMs.toSeconds(),
                         transformedDataDescriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.metadata}>" })
        } else {
            logger.info("Finished transforming <{}> <{}, {}> <{} result(s)> in <{} s>: [{}]",
                        transformerId,
                        targetMediaType,
                        parameters,
                        transformedDataDescriptors.size,
                        measuredTimeMs.toSeconds(),
                        transformedDataDescriptors.joinToString(", ") { "<${it.metadata}>" })
        }
    }

    private fun generateExceptionDescriptor(transformerId: String,
                                            targetMediaType: MediaType,
                                            parameters: Parameters,
                                            dataDescriptors: List<DataDescriptor>): String =
            "<:1> <:2, :3> <:4 source(s)>: [:5]"
                    .replace(":1", transformerId)
                    .replace(":2", targetMediaType.toString())
                    .replace(":3", parameters.toString())
                    .replace(":4", dataDescriptors.size.toString())
                    .replace(":5", dataDescriptors.getLocationsInString())

    private fun List<DataDescriptor>.getLocationsInString(): String =
            joinToString(", ") {
                try {
                    "<${it.data.getLocation()}, ${it.mediaType}>"
                } catch (e: UnsupportedOperationException) {
                    "<no location, ${it.mediaType}>"
                }
            }
}