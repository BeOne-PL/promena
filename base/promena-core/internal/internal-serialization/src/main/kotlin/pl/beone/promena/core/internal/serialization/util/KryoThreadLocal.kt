package pl.beone.promena.core.internal.serialization.util

import com.esotericsoftware.kryo.Kryo

object KryoThreadLocal {

    @JvmStatic
    val instance: ThreadLocal<Kryo> by lazy {
        object : ThreadLocal<Kryo>() {
            override fun initialValue(): Kryo =
                createKryo()
        }
    }
}