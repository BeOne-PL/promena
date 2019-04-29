package pl.beone.promena.core.contract.transformation

import pl.beone.promena.core.contract.communication.CommunicationParameters

interface TransformationUseCase {

    fun transform(transformerId: String, bytes: ByteArray, communicationParameters: CommunicationParameters): ByteArray

}