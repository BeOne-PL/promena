package pl.beone.promena.transformer.internal.model.data

import pl.beone.promena.transformer.contract.model.Data
import java.io.InputStream
import java.net.URI

object NoData : Data {

    override fun getBytes(): ByteArray {
        throw createException()
    }

    override fun getInputStream(): InputStream {
        throw createException()
    }

    override fun getLocation(): URI {
        throw createException()
    }

    override fun isAccessible() {
        throw createException()
    }

    override fun delete() {
        throw createException()
    }

    private fun createException(): UnsupportedOperationException =
        UnsupportedOperationException("This resource has no content")
}