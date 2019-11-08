package ${package}.util

import io.mockk.mockk
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.contract.communication.CommunicationWritableDataCreator
import pl.beone.promena.transformer.contract.model.data.WritableData
import pl.beone.promena.transformer.internal.model.data.memory.emptyMemoryWritableData
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

internal object MemoryCommunicationWritableDataCreator : CommunicationWritableDataCreator {
    override fun create(communicationParameters: CommunicationParameters): WritableData = emptyMemoryWritableData()
}

internal fun create${pascalCaseTransformerId}Transformer(
    settings: ${pascalCaseTransformerId}TransformerSettings = ${pascalCaseTransformerId}TransformerSettings(),
    parameters: ${pascalCaseTransformerId}TransformerDefaultParameters = ${pascalCaseTransformerId}TransformerDefaultParameters()
): ${pascalCaseTransformerId}Transformer =
    ${pascalCaseTransformerId}Transformer(settings, parameters, mockk(), MemoryCommunicationWritableDataCreator)