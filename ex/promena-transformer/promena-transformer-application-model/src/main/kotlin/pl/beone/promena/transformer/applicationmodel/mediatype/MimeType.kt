package pl.beone.promena.transformer.applicationmodel.mediatype

import javax.activation.MimeType

class MimeType(rawdata: String) : MimeType(rawdata) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is pl.beone.promena.transformer.applicationmodel.mediatype.MimeType) return false

        if (primaryType != other.primaryType) return false
        if (subType != other.subType) return false
        if (parameters != other.parameters) return false

        return true
    }

    override fun hashCode(): Int {
        var result = primaryType.hashCode()
        result = 31 * result + subType.hashCode()
        result = 31 * result + parameters.hashCode()
        return result
    }
}