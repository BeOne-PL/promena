package pl.beone.promena.core.internal.serialization

import com.esotericsoftware.kryo.Kryo
import de.javakaffee.kryoserializers.*
import org.objenesis.strategy.StdInstantiatorStrategy
import java.lang.reflect.InvocationHandler
import java.net.URI
import java.util.*

fun createKryo(): Kryo =
    Kryo().apply {
        setInstantiatorStrategyForNoArgsConstructors()
        registerSerializers()
    }

private fun Kryo.setInstantiatorStrategyForNoArgsConstructors() {
    instantiatorStrategy = Kryo.DefaultInstantiatorStrategy(StdInstantiatorStrategy())
}

private fun Kryo.registerSerializers() {
    // kryo-serializers
    register(Collections.EMPTY_LIST::class.java, CollectionsEmptyListSerializer())
    register(Collections.EMPTY_MAP::class.java, CollectionsEmptyMapSerializer())
    register(Collections.EMPTY_SET::class.java, CollectionsEmptySetSerializer())
    register(Arrays.asList("")::class.java, ArraysAsListSerializer())
    register(Collections.singleton("")::class.java, CollectionsSingletonSetSerializer())
    register(Collections.singletonMap("", "")::class.java, CollectionsSingletonMapSerializer())
    register(GregorianCalendar::class.java, GregorianCalendarSerializer())
    register(InvocationHandler::class.java, JdkProxySerializer())
    UnmodifiableCollectionsSerializer.registerSerializers(this)
    SynchronizedCollectionsSerializer.registerSerializers(this)

    register(URI::class.java, URISerializer())
}