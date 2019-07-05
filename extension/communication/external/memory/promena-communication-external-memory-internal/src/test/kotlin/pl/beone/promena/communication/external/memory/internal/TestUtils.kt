package pl.beone.promena.communication.external.memory.internal

import pl.beone.promena.transformer.internal.model.data.InMemoryData

internal fun String.createInMemoryData(): InMemoryData =
        InMemoryData(this.toByteArray())