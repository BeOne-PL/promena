package pl.beone.promena.communication.memory.model.contract

import pl.beone.promena.transformer.contract.communication.CommunicationParameters

interface MemoryCommunicationParameters : CommunicationParameters {

    companion object {
        const val ID = "memory"
    }
}