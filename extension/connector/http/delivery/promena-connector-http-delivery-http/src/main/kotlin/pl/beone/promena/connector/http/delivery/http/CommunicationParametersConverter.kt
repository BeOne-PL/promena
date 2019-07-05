package pl.beone.promena.connector.http.delivery.http

import org.springframework.util.MultiValueMap
import pl.beone.promena.transformer.contract.communication.CommunicationParameters
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters

internal class CommunicationParametersConverter {

    fun convert(parameters: MultiValueMap<String, String>): CommunicationParameters =
            MapCommunicationParameters(parameters.entries
                                               .map { (key, values) -> key to convert(values) }
                                               .toMap())

    private fun convert(values: List<Any>): Any =
            unWrapIfArrayContainsOnlyOneElement(
                    values.map { it.convertToBoolean() ?: it.convertToDouble() ?: it.convertToLong() ?: it.toString() }
            )

    private fun Any.convertToBoolean(): Boolean? =
            when (this.toString()) {
                "true"  -> true
                "false" -> false
                else    -> null
            }

    private fun Any.convertToDouble(): Double? =
            try {
                val string = this.toString()
                if (string.contains(".")) {
                    string.toDouble()
                } else {
                    null
                }
            } catch (e: NumberFormatException) {
                null
            }

    private fun Any.convertToLong(): Long? =
            this.toString().toLongOrNull()

    private fun unWrapIfArrayContainsOnlyOneElement(it: List<Any>): Any =
            if (it.size == 1) it.first() else it
}