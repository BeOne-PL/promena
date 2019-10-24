package pl.beone.promena.alfresco.module.core.internal.transformation.post

import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PostTransformationExecutorValidationException
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import java.lang.reflect.Field

class SerializationPostTransformationExecutorValidator :
    PostTransformationExecutorValidator {

    override fun validate(postTransformationExecutor: PostTransformationExecutor) {
        if (postTransformationExecutor.javaClass.isAnonymousClass) {
            throw PostTransformationExecutorValidationException("Can't be anonymous class")
        }

        val proxyFields = postTransformationExecutor.javaClass.declaredFields.filter { checkIfIsProxy(postTransformationExecutor, it) }
        if (proxyFields.isNotEmpty()) {
            throw PostTransformationExecutorValidationException("Can't contain beans but has: ${proxyFields.map(Field::getName)}")
        }
    }

    private fun checkIfIsProxy(postTransformationExecutor: PostTransformationExecutor, field: Field): Boolean {
        val currentAccessible = field.isAccessible
        try {
            field.isAccessible = true
            val name = field.get(postTransformationExecutor).javaClass.name
            return name.startsWith("com.sun.proxy") && name.contains("Proxy")
        } finally {
            field.isAccessible = currentAccessible
        }
    }
}