package pl.beone.promena.alfresco.module.core.internal.transformation.post

import com.esotericsoftware.kryo.KryoException
import org.slf4j.Logger
import pl.beone.promena.alfresco.module.core.applicationmodel.exception.PostTransformationExecutorValidationException
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutor
import pl.beone.promena.alfresco.module.core.contract.transformation.post.PostTransformationExecutorValidator
import pl.beone.promena.core.applicationmodel.exception.serializer.SerializationException
import pl.beone.promena.core.contract.serialization.SerializationService
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class SerializationPostTransformationExecutorValidator(
    private val serializationService: SerializationService
) :
    PostTransformationExecutorValidator {

    override fun validate(postTransformationExecutor: PostTransformationExecutor) {
        try {
            validateAnonymousClass(postTransformationExecutor)

            val fields = postTransformationExecutor.javaClass.declaredFields.toList()

            validateConnectionsWithClassLoader(postTransformationExecutor, fields)
            validateNonStaticLoggers(postTransformationExecutor, fields)
        } catch (e: Exception) {
            if (e is PostTransformationExecutorValidationException) {
                throw e
            } else {
                throw PostTransformationExecutorValidationException("PostTransformationExecutor isn't correct", e)
            }
        }
    }

    private fun validateAnonymousClass(postTransformationExecutor: PostTransformationExecutor) {
        if (postTransformationExecutor.javaClass.isAnonymousClass) {
            throw PostTransformationExecutorValidationException("PostTransformationExecutor implementation can't be an anonymous class")
        }
    }

    private fun validateNonStaticLoggers(postTransformationExecutor: PostTransformationExecutor, fields: List<Field>) {
        val notStaticFields = fields.filter { checkIfItContainsNonStaticLogger(postTransformationExecutor, it) }
        if (notStaticFields.isNotEmpty()) {
            throw PostTransformationExecutorValidationException(
                "PostTransformationExecutor can't contain non-static logger fields but has: ${notStaticFields.map(Field::getName)}"
            )
        }
    }

    private fun checkIfItContainsNonStaticLogger(postTransformationExecutor: PostTransformationExecutor, field: Field): Boolean =
        makeAccessible(field) {
            if (field.get(postTransformationExecutor) is Logger) {
                !Modifier.isStatic(field.modifiers)
            } else {
                false
            }
        }

    private fun validateConnectionsWithClassLoader(postTransformationExecutor: PostTransformationExecutor, fields: List<Field>) {
        val connectedWithClassLoaderFields = fields.filter { checkIfItHasConnectionWithClassLoader(postTransformationExecutor, it) }
        if (connectedWithClassLoaderFields.isNotEmpty()) {
            throw PostTransformationExecutorValidationException(
                "PostTransformationExecutor can't contain fields with reference to ClassLoader (for example: proxy class, object with BeanFactory field etc.) but has: " +
                        "${connectedWithClassLoaderFields.map(Field::getName)}"
            )
        }
    }

    private fun checkIfItHasConnectionWithClassLoader(postTransformationExecutor: PostTransformationExecutor, field: Field): Boolean =
        try {
            makeAccessible(field) {
                serializationService.serialize(field.get(postTransformationExecutor))
                false
            }
        } catch (e: SerializationException) {
            if (
                e.cause is KryoException &&
                e.cause!!.cause is IllegalArgumentException &&
                e.cause!!.cause!!.message?.contains("ClassLoaders") == true
            ) {
                true
            } else {
                throw e
            }
        }

    @Suppress("DEPRECATION")
    private fun <T> makeAccessible(field: Field, toRun: () -> T): T {
        val currentAccessible = field.isAccessible
        return try {
            field.isAccessible = true
            toRun()
        } finally {
            field.isAccessible = currentAccessible
        }
    }
}