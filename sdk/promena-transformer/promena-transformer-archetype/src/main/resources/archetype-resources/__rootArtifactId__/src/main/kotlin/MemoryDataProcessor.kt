package ${package}

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.MemoryData

internal class MemoryDataProcessor {

    fun transform(data: Data): MemoryData =
            data.convertToString()
                    .addHashAtTheEnd()
                    .toMemoryData()

    private fun Data.convertToString(): String =
            String(this.getBytes())

    private fun String.addHashAtTheEnd(): String =
            "$this#"

    private fun String.toMemoryData(): MemoryData =
            MemoryData(toByteArray())
}