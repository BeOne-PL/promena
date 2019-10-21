package pl.beone.promena.alfresco.module.core.applicationmodel.exception

class TransformationStoppedException(
    cause: Throwable
) : RuntimeException("Transformation has been stopped", cause)