package pl.beone.promena.core.configuration.external.akka.framework.serialization

import io.altoo.akka.serialization.kryo.DefaultKryoInitializer
import io.altoo.akka.serialization.kryo.serializer.scala.ScalaKryo
import pl.beone.promena.core.internal.serialization.extension.registerSerializers

class AkkaAdditionalSerializersKryoInitializer : DefaultKryoInitializer() {

    override fun postInit(kryo: ScalaKryo) {
        kryo.registerSerializers()
    }
}