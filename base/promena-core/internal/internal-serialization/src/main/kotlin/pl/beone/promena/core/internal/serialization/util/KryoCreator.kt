package pl.beone.promena.core.internal.serialization.util

import com.esotericsoftware.kryo.Kryo
import pl.beone.promena.core.internal.serialization.extension.registerSerializers
import pl.beone.promena.core.internal.serialization.extension.setInstantiatorStrategyForNoArgsConstructors

internal fun createKryo(classLoader: ClassLoader? = null): Kryo =
    Kryo()
        .apply { classLoader?.let { this.classLoader = it } }
        .setInstantiatorStrategyForNoArgsConstructors()
        .registerSerializers()