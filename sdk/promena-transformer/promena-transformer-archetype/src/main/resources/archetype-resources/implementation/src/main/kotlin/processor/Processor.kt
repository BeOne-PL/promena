package ${package}.processor

import kotlinx.coroutines.asCoroutineDispatcher
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.data.singleTransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.model.data.WritableData
import pl.beone.promena.transformer.internal.extension.copy
import pl.beone.promena.transformer.util.execute
import java.util.concurrent.Executors
import ${package}.${pascalCaseTransformerId}TransformerSettings
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters

internal class Processor(
    private val settings: ${pascalCaseTransformerId}TransformerSettings,
    private val defaultParameters: ${pascalCaseTransformerId}TransformerDefaultParameters
) {

    private val executionDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    fun process(
        singleDataDescriptor: DataDescriptor.Single,
        parameters: Parameters,
        transformedWritableData: WritableData
    ): TransformedDataDescriptor.Single {
        val (data, _, metadata) = singleDataDescriptor

        execute(parameters.getTimeoutOrNull() ?: defaultParameters.timeout, executionDispatcher) {
            transformedWritableData.copy(data.getInputStream())
        }

        return singleTransformedDataDescriptor(transformedWritableData, metadata)
    }
}