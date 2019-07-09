package pl.beone.promena.communication.external.memory.internal

import pl.beone.promena.transformer.internal.model.data.MemoryData

internal fun String.toMemoryData(): MemoryData =
        MemoryData(this.toByteArray())