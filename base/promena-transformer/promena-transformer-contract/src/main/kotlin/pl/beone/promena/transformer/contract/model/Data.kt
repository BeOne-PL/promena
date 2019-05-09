package pl.beone.promena.transformer.contract.model

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import java.net.URI

interface Data {

    fun getBytes(): ByteArray

    fun getLocation(): URI

    @Throws(DataAccessibilityException::class)
    fun isAvailable()
}