package pl.beone.promena.lib.http.client.contract.request

import pl.beone.promena.core.contract.communication.CommunicationParameters

interface TransformationExecutor {

    fun execute(transformerId: String, bytes: ByteArray, parameters: CommunicationParameters, timeout: Long): ByteArray
}