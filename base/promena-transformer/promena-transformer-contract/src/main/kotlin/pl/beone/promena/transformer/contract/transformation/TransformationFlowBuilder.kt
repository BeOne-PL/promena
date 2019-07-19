package pl.beone.promena.transformer.contract.transformation

class TransformationFlowBuilder {

    private val transformers = ArrayList<TransformationFlow.Single>()

    fun add(transformer: TransformationFlow.Single): TransformationFlowBuilder {
        transformers.add(transformer)

        return this
    }

    fun build(): TransformationFlow =
            transformationFlow(transformers)
}