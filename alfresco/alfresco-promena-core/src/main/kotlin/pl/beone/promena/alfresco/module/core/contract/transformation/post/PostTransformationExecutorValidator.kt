package pl.beone.promena.alfresco.module.core.contract.transformation.post

interface PostTransformationExecutorValidator {

    fun validate(postTransformationExecutor: PostTransformationExecutor)
}