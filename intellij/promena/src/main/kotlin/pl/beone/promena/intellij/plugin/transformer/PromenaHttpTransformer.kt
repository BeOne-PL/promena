package pl.beone.promena.intellij.plugin.transformer

import pl.beone.lib.promena.connector.http.external.AbstractPromenaHttpTransformer
import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

internal class PromenaHttpTransformer(
    serializationService: SerializationService
) : AbstractPromenaHttpTransformer(serializationService, httpClient) {

    companion object {
        private val httpClient = HttpClient.create()
    }

    fun transform(httpAddress: String, transformationDescriptor: TransformationDescriptor): Mono<PerformedTransformationDescriptor> =
        transform(transformationDescriptor, httpAddress)
}
