package pl.beone.promena.core.external.akka.actor.serializer.message

data class DeserializedMessage<T>(
    val element: T
)