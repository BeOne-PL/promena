package pl.beone.promena.alfresco.module.core.applicationmodel.exception

class PotentialOutOfScopeVariableException(
    cause: Throwable
) : RuntimeException("It's highly probable that your implementation of PostTransformationExecution uses out of scope variable", cause)