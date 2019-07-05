package pl.beone.promena.transformer.contract.model

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import java.net.URI

interface Data {

    @Throws(DataReadException::class, DataAccessibilityException::class)
    fun getBytes(): ByteArray

    @Throws(UnsupportedOperationException::class)
    fun getLocation(): URI

    @Throws(DataAccessibilityException::class)
    fun isAccessible()

    @Throws(DataDeleteException::class, UnsupportedOperationException::class)
    fun delete()
}