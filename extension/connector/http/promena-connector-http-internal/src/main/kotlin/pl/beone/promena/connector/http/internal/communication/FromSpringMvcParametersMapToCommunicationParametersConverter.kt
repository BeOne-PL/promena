package pl.beone.promena.connector.http.internal.communication

import pl.beone.promena.connector.http.contract.communication.FromMapToCommunicationParametersConverter
import pl.beone.promena.core.contract.communication.CommunicationParameters
import pl.beone.promena.core.internal.communication.MapCommunicationParameters

class FromSpringMvcParametersMapToCommunicationParametersConverter : FromMapToCommunicationParametersConverter {

    override fun convert(parameters: Map<String, String>): CommunicationParameters =
            MapCommunicationParameters(parameters.entries
                                               .map { (key, value) -> key to convert(value) }
                                               .toMap())

    private fun convert(value: Any): Any =
            value.convertToBoolean() ?: value.convertToDouble() ?: value.convertToLong() ?: value.toString()

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
}