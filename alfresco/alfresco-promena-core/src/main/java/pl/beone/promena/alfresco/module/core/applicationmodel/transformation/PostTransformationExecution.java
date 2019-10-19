package pl.beone.promena.alfresco.module.core.applicationmodel.transformation;

import org.alfresco.service.ServiceRegistry;

@FunctionalInterface
public interface PostTransformationExecution {

    void execute(ServiceRegistry serviceRegistry, TransformationExecutionResult result);
}
