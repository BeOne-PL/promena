@file:Suppress("UNCHECKED_CAST")

package pl.beone.lib.typeconverter.internal

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.lib.typeconverter.internal.JavaTypesUtils.*
import java.net.URI

fun <T> Any.castOrConvert(clazz: Class<T>): T {
    return try {
        clazz.cast(this)
    } catch (e: Exception) {
        this.convert(clazz)
    }
}

private fun <T> Any.convert(clazz: Class<T>): T {
    try {
        return when {
            clazz == Boolean::class || clazz == Boolean::class.java || isBoolean(clazz) -> toBoolean(this) as T

            clazz == String::class || clazz == String::class.java || isString(clazz)    -> this.toString() as T

            clazz == Long::class || clazz == Long::class.java || isLong(clazz)          -> this.toString().toLong() as T
            clazz == Int::class || clazz == Int::class.java || isInteger(clazz)         -> this.toString().toInt() as T

            clazz == Double::class || clazz == Double::class.java || isDouble(clazz)    -> this.toString().toDouble() as T
            clazz == Float::class || clazz == Float::class.java || isFloat(clazz)       -> this.toString().toFloat() as T

            clazz == URI::class || clazz == URI::class.java || isURI(clazz)             -> URI(this.toString()) as T

            else                                                                        ->
                throw TypeConversionException("Converting from <${this::class.java.name}> to <${clazz.name}> isn't supported")
        }
    } catch (e: TypeConversionException) {
        throw e
    } catch (e: Exception) {
        throw TypeConversionException("Couldn't convert <$this> (${this::class.java.name}) to <${clazz.name}>")
    }
}

private fun toBoolean(value: Any): Boolean =
    when (value.toString()) {
        "true"  -> true
        "false" -> false
        else    -> throw Exception("Boolean must be <true> or <false> but is <$value>")
    }

inline fun <reified T : Any> getClazz(): Class<T> =
    T::class.java