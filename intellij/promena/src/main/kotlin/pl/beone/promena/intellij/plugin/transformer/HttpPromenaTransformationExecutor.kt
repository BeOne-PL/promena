package pl.beone.promena.intellij.plugin.transformer

import pl.beone.promena.core.applicationmodel.transformation.PerformedTransformationDescriptor
import pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor
import pl.beone.promena.core.contract.serialization.SerializationService
import pl.beone.promena.intellij.plugin.applicationmodel.HttpConfigurationParameters
import pl.beone.promena.lib.connector.http.external.HttpPromenaTransformer

internal class HttpPromenaTransformationExecutor(
    serializationService: SerializationService
) {

    private val httpPromenaTransformer = HttpPromenaTransformer(serializationService)

    suspend fun transform(
        transformationDescriptor: TransformationDescriptor,
        httpConfigurationParameters: HttpConfigurationParameters
    ): PerformedTransformationDescriptor =
        httpPromenaTransformer.execute(transformationDescriptor, httpConfigurationParameters.getAddress())
}
