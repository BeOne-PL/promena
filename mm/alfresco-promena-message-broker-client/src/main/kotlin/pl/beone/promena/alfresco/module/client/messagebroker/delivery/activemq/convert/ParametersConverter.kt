package pl.beone.promena.alfresco.module.client.messagebroker.delivery.activemq.convert

import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

class ParametersConverter {

    fun convert(parameters: Map<String, Any>): Parameters =
            MapParameters(parameters)
}