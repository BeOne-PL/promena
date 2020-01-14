package pl.beone.promena.transformer.contract.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.applicationmodel.exception.data.DataReadException
import java.io.InputStream
import java.net.URI

/**
 * Represents a data abstraction participating in a transformation.
 */
interface Data {

    /**
     * @throws DataReadException if an error has occurred while reading data
     * @throws DataAccessibilityException if the data isn't accessible
     */
    fun getBytes(): ByteArray

    /**
     * @throws DataAccessibilityException if the data isn't accessible
     */
    fun getInputStream(): InputStream

    /**
     * @throws UnsupportedOperationException if the data can't be represented by URI
     */
    fun getLocation(): URI

    /**
     * @throws DataAccessibilityException if the data isn't accessible
     */
    fun isAccessible()

    /**
     * @throws DataDeleteException if an error has occurred while deleting data
     * @throws UnsupportedOperationException if the data isn't a removable resource
     */
    fun delete()
}