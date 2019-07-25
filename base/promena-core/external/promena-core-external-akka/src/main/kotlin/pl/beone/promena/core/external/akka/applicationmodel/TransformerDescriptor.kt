package pl.beone.promena.core.external.akka.applicationmodel

import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.transformer.TransformerId

data class TransformerDescriptor(val transformerId: TransformerId,
                                 val transformer: Transformer)