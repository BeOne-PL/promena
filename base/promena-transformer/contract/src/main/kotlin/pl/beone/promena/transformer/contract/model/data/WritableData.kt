package pl.beone.promena.transformer.contract.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import java.io.OutputStream

/**
 * Extends [Data] with the ability of modifying data.
 */
interface WritableData : Data {

    /**
     * @throws DataAccessibilityException if the data isn't accessible
     */
    fun getOutputStream(): OutputStream
}