package pl.beone.promena.alfresco.module.core.external.transformation.post

import io.mockk.mockk
import org.junit.Test

import org.junit.Assert.*

class ReflectionPostTransformationExecutorInjectorTest {

    companion object {
        private val postTransformationExecutorInjector = ReflectionPostTransformationExecutorInjector(mockk(), mockk())
    }

    @Test
    fun inject() {
    }
}