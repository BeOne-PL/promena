package pl.beone.promena.alfresco.module.core.applicationmodel.transformation;

import org.alfresco.service.ServiceRegistry;
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor;
import pl.beone.promena.transformer.contract.transformation.Transformation;

@FunctionalInterface
public interface PostTransformationExecution {

    void execute(Transformation transformation, NodeDescriptor nodeDescriptor, ServiceRegistry serviceRegistry, TransformationExecutionResult result);
}
