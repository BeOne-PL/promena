package pl.beone.lib.jms.message.converter

import org.springframework.jms.support.converter.MessageConverter
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import javax.jms.BytesMessage
import javax.jms.Message
import javax.jms.Session

class KryoMessageConverter(private val kryoSerializationService: KryoSerializationService) : MessageConverter {

    companion object {
        private const val PROPERTY_SERIALIZATION_CLASS = "serialization_class"
    }

    override fun toMessage(obj: Any, session: Session): Message =
            session.createBytesMessage().apply {
                writeBytes(kryoSerializationService.serialize(obj))
                setStringProperty(PROPERTY_SERIALIZATION_CLASS, obj.javaClass.name)
            }

    override fun fromMessage(message: Message): Any {
        if (message !is BytesMessage) {
            throw IllegalArgumentException("This implementation supports only <javax.jms.BytesMessage> but received <${message.javaClass.canonicalName}>")
        }

        val bytes = message.getBytes()
        val clazz = message.getClassFromProperties()

        return kryoSerializationService.deserialize(bytes, clazz)
    }

    private fun BytesMessage.getBytes(): ByteArray {
        val byteArray = ByteArray(bodyLength.toInt())
        readBytes(byteArray)
        return byteArray
    }

    private fun Message.getClassFromProperties(): Class<*> =
            try {
                Class.forName(getStringProperty(PROPERTY_SERIALIZATION_CLASS))
                        ?: throw NoSuchElementException("Properties doesn't contain <$PROPERTY_SERIALIZATION_CLASS> value")
            } catch (e: ClassNotFoundException) {
                throw IllegalArgumentException("Class determined in <$PROPERTY_SERIALIZATION_CLASS> message property isn't available", e)
            }

}