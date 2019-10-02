@file:Suppress("FunctionName", "UNCHECKED_CAST")

package pl.beone.promena.transformer.internal.model.extensions

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.lib.typeconverter.internal.castOrConvert
import pl.beone.lib.typeconverter.internal.getClazz

internal fun Map<String, Any>._get(key: String): Any =
    this[key] ?: throw NoSuchElementException("There is no <$key> element")

internal fun Map<String, Any>.getOrNull(key: String): Any? =
    this[key]

internal fun <T> Map<String, Any>.get(key: String, clazz: Class<T>): T =
    _get(key).castOrConvert(clazz)

internal fun <T> Map<String, Any>.getOrNull(key: String, clazz: Class<T>): T? =
    try {
        _get(key).castOrConvert(clazz)
    } catch (e: NoSuchElementException) {
        null
    }

internal fun <T> Map<String, Any>.getOrDefault(key: String, clazz: Class<T>, default: T): T =
    try {
        _get(key).castOrConvert(clazz)
    } catch (e: NoSuchElementException) {
        default
    }

internal fun Map<String, Any>.getListWithoutType(key: String): List<Any> =
    get(key, getClazz())

internal fun Map<String, Any>.getListWithoutTypeOrNull(key: String): List<Any>? =
    getListOrNull(key, getClazz())

internal fun Map<String, Any>.getListWithoutTypeOrDefault(key: String, default: List<Any>): List<Any> =
    getListOrDefault(key, getClazz(), default)

internal fun <T> Map<String, Any>.getList(key: String, clazz: Class<T>): List<T> {
    val list = getListWithoutType(key)

    return getListWithoutType(key).map {
        try {
            it.castOrConvert(clazz)
        } catch (e: TypeConversionException) {
            throw TypeConversionException("Couldn't convert <$list> to List<${clazz.name}>")
        }
    }
}

internal fun <T> Map<String, Any>.getListOrNull(key: String, clazz: Class<T>): List<T>? =
    try {
        getList(key, clazz)
    } catch (e: NoSuchElementException) {
        null
    }

internal fun <T> Map<String, Any>.getListOrDefault(key: String, clazz: Class<T>, default: List<T>): List<T> =
    try {
        getList(key, clazz)
    } catch (e: NoSuchElementException) {
        default
    }