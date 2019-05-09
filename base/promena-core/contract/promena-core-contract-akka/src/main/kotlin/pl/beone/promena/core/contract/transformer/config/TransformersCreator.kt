package pl.beone.promena.core.contract.transformer.config

import pl.beone.promena.core.applicationmodel.akka.actor.ActorRefWithId
import pl.beone.promena.transformer.contract.Transformer

interface TransformersCreator {

    fun create(transformers: List<Transformer>): List<ActorRefWithId>
}