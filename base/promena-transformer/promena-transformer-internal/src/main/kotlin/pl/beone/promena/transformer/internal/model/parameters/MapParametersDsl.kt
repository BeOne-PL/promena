package pl.beone.promena.transformer.internal.model.parameters

fun parameters(): MapParameters =
        MapParameters.empty()

infix fun MapParameters.add(entry: Pair<String, Any>): MapParameters =
        MapParameters.of(getAll() + entry)