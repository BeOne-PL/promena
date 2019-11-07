package pl.beone.promena.transformer.contract.model.data

import pl.beone.promena.transformer.applicationmodel.exception.data.DataAccessibilityException
import java.io.OutputStream

interface WritableData : Data {

    @Throws(DataAccessibilityException::class)
    fun getOutputStream(): OutputStream
}