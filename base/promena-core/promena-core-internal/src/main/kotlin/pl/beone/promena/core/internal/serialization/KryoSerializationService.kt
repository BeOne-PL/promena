package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.ByteBufferInput
import com.esotericsoftware.kryo.io.ByteBufferOutput
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import pl.beone.promena.core.applicationmodel.exception.serializer.DeserializationException
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException
import pl.beone.promena.core.contract.serialization.SerializationService
import java.lang.reflect.InvocationHandler
import java.net.URI
import java.util.*

// default = 100MB
class KryoSerializationService(private val bufferSize: Int = 100 * 1024 * 1024) : SerializationService {

    companion object {
        private val kryoThreadLocal = object : ThreadLocal<Kryo>() {
            override fun initialValue(): Kryo =
                    Kryo().apply {
                        // classes with no-args constructors
                        val instStrategy =
                                instantiatorStrategy.newInstantiatorOf(Kryo.DefaultInstantiatorStrategy::class.java).newInstance()
                        instStrategy.fallbackInstantiatorStrategy = StdInstantiatorStrategy()
                        instantiatorStrategy = instStrategy

                        // custom serializers
                        register(Arrays.asList("")::class.java, ArraysAsListSerializer())
                        register(Collections.EMPTY_LIST::class.java, CollectionsEmptyListSerializer())
                        register(Collections.EMPTY_MAP::class.java, CollectionsEmptyMapSerializer())
                        register(Collections.EMPTY_SET::class.java, CollectionsEmptySetSerializer())
                        register(Collections.singletonList("")::class.java, CollectionsSingletonListSerializer())
                        register(Collections.singleton("")::class.java, CollectionsSingletonSetSerializer())
                        register(Collections.singletonMap("", "")::class.java, CollectionsSingletonMapSerializer())
                        register(GregorianCalendar::class.java, GregorianCalendarSerializer())
                        register(InvocationHandler::class.java, JdkProxySerializer())
                        UnmodifiableCollectionsSerializer.registerSerializers(this)
                        SynchronizedCollectionsSerializer.registerSerializers(this)

                        register(URI::class.java, URISerializer())
                    }
        }


    }

    override fun <T> serialize(element: T): ByteArray =
            try {
                with(ByteBufferOutput(bufferSize)) {
                    kryoThreadLocal.get().writeClassAndObject(this, element)
                    return this.toBytes()
                }
            } catch (e: Exception) {
                throw SerializationException("Couldn't serialize", e)
            }

    @Suppress("UNCHECKED_CAST")
    override fun <T> deserialize(bytes: ByteArray, clazz: Class<T>): T =
            try {
                kryoThreadLocal.get().readClassAndObject(ByteBufferInput(bytes)) as T
            } catch (e: Exception) {
                throw DeserializationException("Couldn't deserialize", e)
            }
}