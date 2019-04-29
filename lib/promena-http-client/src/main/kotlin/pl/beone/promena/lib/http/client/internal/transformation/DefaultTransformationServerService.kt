package pl.beone.promena.lib.http.client.internal.transformation

import org.slf4j.LoggerFactory
import pl.beone.promena.core.common.utils.*
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.core.internal.communication.MapCommunicationParameters
import pl.beone.promena.lib.http.client.applicationmodel.exception.TransformationException
import pl.beone.promena.lib.http.client.applicationmodel.exception.TransformationTimeoutException
import pl.beone.promena.lib.http.client.contract.request.TransformationExecutor
import pl.beone.promena.lib.http.client.contract.transformation.TransformationServerService
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformationDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters
import java.net.URI
import java.util.concurrent.TimeoutException

class DefaultTransformationServerService(private val transformationExecutor: TransformationExecutor,
                                         private val serializationService: SerializationService,
                                         private val timeout: Long,
                                         private val location: URI?) : TransformationServerService {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultTransformationServerService::class.java)
    }

    override fun transform(transformerId: String,
                           dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters,
                           timeout: Long?): List<TransformedDataDescriptor> {
        logBeforeTransformation(transformerId, dataDescriptors, targetMediaType, parameters)

        val (transformedDataDescriptors, measuredTimeMs) = measureTimeMillisWithContent {
            val exceptionDescriptor = generateExceptionDescriptor(transformerId, targetMediaType, parameters, dataDescriptors)

            val determinedTimeout = determineTimeout(timeout)

            try {
                performTransformation(transformerId, dataDescriptors, targetMediaType, parameters, determinedTimeout)
            } catch (e: TimeoutException) {
                throw TransformationTimeoutException("Couldn't transform because transformation time <$determinedTimeout> has expired | $exceptionDescriptor")
            } catch (e: Exception) {
                throw TransformationException("Couldn't transform because an error occurred | $exceptionDescriptor", e)
            }
        }

        logAfterTransformation(transformerId, targetMediaType, parameters, measuredTimeMs, transformedDataDescriptors)

        return transformedDataDescriptors
    }

    private fun performTransformation(transformerId: String,
                                      dataDescriptors: List<DataDescriptor>,
                                      targetMediaType: MediaType,
                                      parameters: Parameters,
                                      timeout: Long): List<TransformedDataDescriptor> {
        val transformationDescriptor = TransformationDescriptor(dataDescriptors,
                                                                targetMediaType,
                                                                parameters.addTimeout(timeout))

        val bytes = transformationExecutor.execute(transformerId,
                                                   serializationService.serialize(transformationDescriptor),
                                                   createCommunicationParametersWithLocation(location),
                                                   timeout)

        return serializationService.deserialize(bytes, getClazz())
    }

    private fun logBeforeTransformation(transformerId: String,
                                        dataDescriptors: List<DataDescriptor>,
                                        targetMediaType: MediaType,
                                        parameters: Parameters) {
        if (logger.isDebugEnabled) {
            logger.debug("Transforming <{}> <{}, {}> <{} source(s)>: [{}]...",
                         transformerId,
                         targetMediaType,
                         parameters.getAll(),
                         dataDescriptors.size,
                         dataDescriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.mediaType}>" })
        } else {
            logger.info("Transforming <{}> <{}, {}> <{} source(s)>: [{}]...",
                        transformerId,
                        targetMediaType,
                        parameters.getAll(),
                        dataDescriptors.size,
                        dataDescriptors.joinToString(", ") { "<${it.mediaType}>" })
        }
    }

    private fun determineTimeout(timeout: Long?): Long =
            when (timeout) {
                null -> this.timeout
                0L   -> Long.MAX_VALUE
                else -> timeout
            }

    private fun Parameters.addTimeout(timeout: Long): Parameters =
            MapParameters(this.getAll() + mapOf("timeout" to timeout))

    private fun createCommunicationParametersWithLocation(location: URI?): CommunicationParameters =
            MapCommunicationParameters(if (location == null) emptyMap() else mapOf("location" to location.toString()))

    private fun logAfterTransformation(transformerId: String,
                                       targetMediaType: MediaType,
                                       parameters: Parameters,
                                       measuredTimeMs: Long,
                                       transformedDataDescriptors: List<TransformedDataDescriptor>) {
        if (logger.isDebugEnabled) {
            logger.debug("Transformed <{}> <{}, {}> <{} result(s)> in <{} s>: [{}]",
                         transformerId,
                         targetMediaType,
                         parameters.getAll(),
                         transformedDataDescriptors.size,
                         measuredTimeMs.toSeconds(),
                         transformedDataDescriptors.joinToString(", ") { "<${it.data.getBytes().toMB().format(2)} MB, ${it.metadata}>" })
        } else {
            logger.info("Transformed <{}> <{}, {}> <{} result(s)> in <{} s>: [{}]",
                        transformerId,
                        targetMediaType,
                        parameters.getAll(),
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
                    .replace(":3", parameters.getAll().toString())
                    .replace(":4", dataDescriptors.size.toString())
                    .replace(":5", dataDescriptors.joinToString(", ") { "<${it.mediaType}>" })
}