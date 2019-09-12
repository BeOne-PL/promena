package pl.beone.promena.core.contract.transformer.config

import pl.beone.promena.core.applicationmodel.akka.actor.TransformerActorDescriptor
import pl.beone.promena.core.applicationmodel.akka.exception.actor.TransformersCreatorValidationException
import pl.beone.promena.transformer.contract.Transformer

interface TransformersCreator {

    @Throws(TransformersCreatorValidationException::class)
    fun create(transformers: List<Transformer>): List<TransformerActorDescriptor>
}