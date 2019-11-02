package pl.beone.promena.alfresco.module.core.internal.transformation.post;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.beone.promena.alfresco.module.core.applicationmodel.node.NodeDescriptor;
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult;
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor;
import pl.beone.promena.transformer.contract.transformation.Transformation;

public class JavaSerializationPostTransformationExecutorValidatorTest {

    public static class FieldAndLoggerPostTransformationExecutorValidatorTest extends PostTransformationExecutor {

        private static Logger logger = LoggerFactory.getLogger(FieldAndLoggerPostTransformationExecutorValidatorTest.class);

        private final String test;

        public FieldAndLoggerPostTransformationExecutorValidatorTest(String test) {
            this.test = test;
        }

        @Override
        public void execute(@NotNull Transformation transformation,
                            @NotNull NodeDescriptor nodeDescriptor,
                            @NotNull TransformationExecutionResult result) {

        }
    }

    public static class NonStaticLoggerPostTransformationExecutorValidatorTest extends PostTransformationExecutor {

        private Logger logger = LoggerFactory.getLogger(NonStaticLoggerPostTransformationExecutorValidatorTest.class);

        @Override
        public void execute(@NotNull Transformation transformation,
                            @NotNull NodeDescriptor nodeDescriptor,
                            @NotNull TransformationExecutionResult result) {

        }
    }
}
