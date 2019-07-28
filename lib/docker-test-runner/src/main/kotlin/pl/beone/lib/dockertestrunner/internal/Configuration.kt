package pl.beone.lib.dockertestrunner.internal

import pl.beone.lib.typeconverter.internal.castOrConvert
import java.util.*

class Configuration {

    private val baseProperties = Properties().apply {
        load("/base-docker-test.properties")
        load("/docker-test.properties")
    }

    private fun Properties.load(resourcePath: String) {
        Configuration::class.java.getResourceAsStream(resourcePath)
            ?.let { load(it) }
    }

    fun getProperty(key: String): String {
        return baseProperties.getProperty(key) ?: throw NoSuchElementException("There is no <$key> element")
    }

    fun <T> getProperty(key: String, clazz: Class<T>): T {
        return getProperty(key).castOrConvert(clazz)
    }
}