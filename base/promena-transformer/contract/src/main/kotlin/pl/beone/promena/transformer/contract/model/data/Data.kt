package pl.beone.promena.transformer.contract.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import java.io.InputStream
import java.net.URI

interface Data {

    @Throws(DataReadException::class, DataAccessibilityException::class)
    fun getBytes(): ByteArray

    @Throws(DataAccessibilityException::class)
    fun getInputStream(): InputStream

    @Throws(UnsupportedOperationException::class)
    fun getLocation(): URI

    @Throws(DataAccessibilityException::class)
    fun isAccessible()

    @Throws(DataDeleteException::class, UnsupportedOperationException::class)
    fun delete()
}