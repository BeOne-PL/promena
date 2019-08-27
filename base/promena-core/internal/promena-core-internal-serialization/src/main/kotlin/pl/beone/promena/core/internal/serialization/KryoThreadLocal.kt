package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import java.lang.reflect.InvocationHandler
import java.net.URI
import java.util.*

object KryoThreadLocal {
    val instance: ThreadLocal<Kryo> by lazy {
        object : ThreadLocal<Kryo>() {
            override fun initialValue(): Kryo =
                Kryo().apply {
                    setInstantiatorStrategyForNoArgsConstructors()

                    // custom serializers
                    register(listOf("")::class.java, ArraysAsListSerializer())
                    register(Collections.EMPTY_LIST::class.java, CollectionsEmptyListSerializer())
                    register(Collections.EMPTY_MAP::class.java, CollectionsEmptyMapSerializer())
                    register(Collections.EMPTY_SET::class.java, CollectionsEmptySetSerializer())
                    register(Collections.singletonList("")::class.java, CollectionsSingletonListSerializer())
                    register(Collections.singleton("")::class.java, CollectionsSingletonSetSerializer())
                    register(Collections.singletonMap("", "")::class.java, CollectionsSingletonMapSerializer())
                    register(GregorianCalendar::class.java, GregorianCalendarSerializer())
                    register(InvocationHandler::class.java, JdkProxySerializer())
                    register(URI::class.java, URISerializer())
                    UnmodifiableCollectionsSerializer.registerSerializers(this)
                    SynchronizedCollectionsSerializer.registerSerializers(this)

                    register(URI::class.java, URISerializer())
                }

            private fun Kryo.setInstantiatorStrategyForNoArgsConstructors() {
                val instStrategy =
                    instantiatorStrategy.newInstantiatorOf(Kryo.DefaultInstantiatorStrategy::class.java).newInstance()
                instStrategy.fallbackInstantiatorStrategy = StdInstantiatorStrategy()
                instantiatorStrategy = instStrategy
            }
        }
    }
}