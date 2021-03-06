package pl.beone.promena.connector.activemq.delivery.jms.message.converter

import org.springframework.jms.support.converter.MessageConverter
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.contract.serialization.SerializationService
import javax.jms.BytesMessage
import javax.jms.Message
import javax.jms.Session

class KryoMessageConverter(
    private val serializationService: SerializationService
) : MessageConverter {

    companion object {
        const val PROPERTY_SERIALIZATION_CLASS = "serialization_class"
    }

    /**
     * Serializes [obj] and adds [PROPERTY_SERIALIZATION_CLASS] property with the type of [obj].
     */
    override fun toMessage(obj: Any, session: Session): Message =
        session.createBytesMessage().apply {
            writeBytes(serializationService.serialize(obj))
            setStringProperty(PROPERTY_SERIALIZATION_CLASS, obj.javaClass.name)
        }

    /**
     * Deserializes [message] to the type from [PROPERTY_SERIALIZATION_CLASS] property.
     */
    override fun fromMessage(message: Message): Any {
        require(message is BytesMessage) {
            "Implementation supports only <javax.jms.BytesMessage> but it received <${message.javaClass.canonicalName}>"
        }

        val clazz = message.getClassFromProperties()

        return try {
            serializationService.deserialize(message.getBytes(), clazz)
        } catch (e: ClassNotFoundException) {
            throwWrappedExceptionIfErrorOccurredForExceptionClass(clazz, e)
        }
    }

    private fun throwWrappedExceptionIfErrorOccurredForExceptionClass(clazz: Class<*>, exception: ClassNotFoundException) {
        if (clazz is Throwable) {
            throw DeserializationException(
                "Couldn't deserialize <${clazz.javaClass.canonicalName}> exception class because some class isn't available in ClassLoader",
                exception
            )
        } else {
            throw exception
        }
    }

    private fun BytesMessage.getBytes(): ByteArray {
        val byteArray = ByteArray(bodyLength.toInt())
        readBytes(byteArray)
        return byteArray
    }

    private fun Message.getClassFromProperties(): Class<*> =
        try {
            Class.forName(getStringProperty(PROPERTY_SERIALIZATION_CLASS))
                ?: throw NoSuchElementException("Properties don't contain <$PROPERTY_SERIALIZATION_CLASS> entry")
        } catch (e: ClassNotFoundException) {
            throw IllegalArgumentException("Class given in <$PROPERTY_SERIALIZATION_CLASS> message property isn't available", e)
        }

}