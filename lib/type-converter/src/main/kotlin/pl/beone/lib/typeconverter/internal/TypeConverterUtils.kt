@file:Suppress("UNCHECKED_CAST")

package pl.beone.lib.typeconverter.internal

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.lib.typeconverter.internal.JavaTypesUtils.*
import java.net.URI

fun <T> castOrConvert(element: Any, clazz: Class<T>): T {
    return try {
        clazz.cast(element)
    } catch (e: Exception) {
        convert(element, clazz)
    }
}

private fun <T> convert(value: Any, clazz: Class<T>): T {
    try {
        return when {
            clazz == Boolean::class || clazz == Boolean::class.java || isBoolean(clazz) -> toBoolean(value) as T

            clazz == String::class || clazz == String::class.java || isString(clazz)    -> value.toString() as T

            clazz == Long::class || clazz == Long::class.java || isLong(clazz)          -> value.toString().toLong() as T
            clazz == Int::class || clazz == Int::class.java || isInteger(clazz)         -> value.toString().toInt() as T

            clazz == Double::class || clazz == Double::class.java || isDouble(clazz)    -> value.toString().toDouble() as T
            clazz == Float::class || clazz == Float::class.java || isFloat(clazz)       -> value.toString().toFloat() as T

            clazz == URI::class || clazz == URI::class.java || isURI(clazz)             -> URI(value.toString()) as T

            else                                                                        ->
                throw TypeConversionException("Converting from <${value::class.java.name}> to <${clazz.name}> isn't supported")
        }
    } catch (e: TypeConversionException) {
        throw e
    } catch (e: Exception) {
        throw TypeConversionException("Couldn't convert <$value> (${value::class.java.name}) to <${clazz.name}>")
    }
}

private fun toBoolean(value: Any): Boolean =
        when (value.toString()) {
            "true"  -> true
            "false" -> false
            else    -> throw Exception("Boolean has to be <true> or <false> but is <$value>")
        }

inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java