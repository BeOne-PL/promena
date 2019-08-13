package ${package}.applicationmodel

import pl.beone.promena.transformer.contract.transformer.transformerId

object ${pascalCaseTransformerId}Constants {

    const val TRANSFORMER_NAME = "${transformerName}"

    const val TRANSFORMER_SUB_NAME = "${transformerSubName}"

    @JvmField
    val TRANSFORMER_ID = transformerId(TRANSFORMER_NAME, TRANSFORMER_SUB_NAME)
}