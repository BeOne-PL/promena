package pl.beone.promena.core.configuration.external.akka.framework.serialization

import com.esotericsoftware.kryo.Kryo
import de.javakaffee.kryoserializers.URISerializer
import java.net.URI

class KryoCustomSerializersInit {

    fun customize(kryo: Kryo) {
        kryo.register(URI::class.java, URISerializer())
    }
}