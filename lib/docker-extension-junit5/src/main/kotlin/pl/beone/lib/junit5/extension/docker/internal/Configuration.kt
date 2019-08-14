package pl.beone.lib.junit5.extension.docker.internal

import org.testcontainers.shaded.org.apache.commons.lang.text.StrSubstitutor
import pl.beone.lib.typeconverter.internal.castOrConvert
import java.util.*

class Configuration {

    private val baseProperties = Properties().apply {
        load("/base-docker-test.properties")
        load("/docker-test.properties")
    }
    private val strSubstitutor = StrSubstitutor(baseProperties)

    private fun Properties.load(resourcePath: String) {
        Configuration::class.java.getResourceAsStream(resourcePath)
            ?.let { load(it) }
    }

    fun getProperty(key: String): String =
        strSubstitutor.replace(baseProperties.getProperty(key) ?: throw NoSuchElementException("There is no <$key> element"))

    fun <T> getProperty(key: String, clazz: Class<T>): T =
        getProperty(key).castOrConvert(clazz)
}