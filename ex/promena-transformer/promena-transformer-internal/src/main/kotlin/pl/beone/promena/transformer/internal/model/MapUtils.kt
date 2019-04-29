@file:Suppress("FunctionName", "UNCHECKED_CAST")

package pl.beone.promena.transformer.internal.model

import pl.beone.promena.transformer.applicationmodel.exception.general.ConversionException
import pl.beone.promena.transformer.internal.JavaTypesUtils.*
import java.net.URI

fun Map<String, Any>._get(key: String): Any =
        this[key] ?: throw NoSuchElementException("There is no <$key> element")

fun <T> Map<String, Any>.get(key: String, clazz: Class<T>): T {
    val element = this._get(key)

    return castOrConvert(element, clazz)
}

fun Map<String, Any>.getListWithoutType(key: String): List<Any> =
        this.get(key, getClazz())

fun <T> Map<String, Any>.getList(key: String, clazz: Class<T>): List<T> {
    val list = this.getListWithoutType(key)

    return list.map {
        try {
            castOrConvert(it, clazz)
        } catch (e: ConversionException) {
            throw ConversionException("Couldn't convert <$list> to List<${clazz.name}>")
        }
    }
}

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
                throw ConversionException("Converting from <${value::class.java.name}> to <${clazz.name}> isn't supported")
        }
    } catch (e: ConversionException) {
        throw e
    } catch (e: Exception) {
        throw ConversionException("Couldn't convert <$value> (${value::class.java.name}) to <${clazz.name}>")
    }
}

private fun toBoolean(value: Any): Boolean =
        when (value.toString()) {
            "true"  -> true
            "false" -> false
            else    -> throw Exception("Boolean has to be <true> or <false> but is <$value>")
        }

private inline fun <reified T : Any> getClazz(): Class<T> =
        T::class.java