package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

data class TransformationExecution internal constructor(
    val id: String
) {

    companion object {
        @JvmStatic
        fun of(id: String): TransformationExecution =
            TransformationExecution(id)
    }
}