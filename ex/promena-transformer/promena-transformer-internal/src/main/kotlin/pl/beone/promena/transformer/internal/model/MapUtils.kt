@file:Suppress("FunctionName", "UNCHECKED_CAST")

package pl.beone.promena.transformer.internal.model

import pl.beone.lib.typeconverter.applicationmodel.exception.TypeConversionException
import pl.beone.lib.typeconverter.internal.castOrConvert
import pl.beone.lib.typeconverter.internal.getClazz

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
        } catch (e: TypeConversionException) {
            throw TypeConversionException("Couldn't convert <$list> to List<${clazz.name}>")
        }
    }
}