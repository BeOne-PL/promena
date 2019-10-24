package pl.beone.promena.alfresco.module.core.contract.transformation.post

interface PostTransformationExecutorInjector {

    fun inject(postTransformationExecutor: PostTransformationExecutor)
}