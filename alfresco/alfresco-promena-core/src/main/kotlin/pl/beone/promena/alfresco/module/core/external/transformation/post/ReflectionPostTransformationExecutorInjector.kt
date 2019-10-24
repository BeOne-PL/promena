package pl.beone.promena.alfresco.module.core.external.transformation.post

import org.alfresco.service.ServiceRegistry
import org.springframework.context.ApplicationContext
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorInjector

class ReflectionPostTransformationExecutorInjector(
    private val applicationContext: ApplicationContext,
    private val serviceRegistry: ServiceRegistry
) : PostTransformationExecutorInjector {

    private val applicationContextField = PostTransformationExecutor::class.java.getDeclaredField("applicationContext")
    private val serviceRegistryField = PostTransformationExecutor::class.java.getDeclaredField("serviceRegistry")

    override fun inject(postTransformationExecutor: PostTransformationExecutor) {
        try {
            applicationContextField.isAccessible = true
            serviceRegistryField.isAccessible = true

            applicationContextField.set(postTransformationExecutor, applicationContext)
            serviceRegistryField.set(postTransformationExecutor, serviceRegistry)
        } finally {
            applicationContextField.isAccessible = false
            serviceRegistryField.isAccessible = false
        }
    }
}