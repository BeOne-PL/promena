package pl.beone.promena.core.external.akka.actor.serializer.message

data class ToDeserializeMessage<T>(
    val bytes: ByteArray,
    val clazz: Class<T>
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ToDeserializeMessage<*>

        if (!bytes.contentEquals(other.bytes)) return false
        if (clazz != other.clazz) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + clazz.hashCode()
        return result
    }

}