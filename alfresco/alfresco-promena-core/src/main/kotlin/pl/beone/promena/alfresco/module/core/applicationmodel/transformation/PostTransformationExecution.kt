package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import org.alfresco.service.ServiceRegistry

@FunctionalInterface
interface PostTransformationExecution {

    fun execute(serviceRegistry: ServiceRegistry, result: TransformationExecutionResult)
}