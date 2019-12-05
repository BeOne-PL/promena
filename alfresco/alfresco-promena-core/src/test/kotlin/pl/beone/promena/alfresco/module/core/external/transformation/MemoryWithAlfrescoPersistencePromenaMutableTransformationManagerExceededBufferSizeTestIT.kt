package pl.beone.promena.alfresco.module.core.external.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrowExactly
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT
import pl.beone.promena.alfresco.module.core.external.transformation.manager.MemoryWithAlfrescoPersistencePromenaMutableTransformationManager
import pl.beone.promena.core.internal.serialization.KryoSerializationService
import java.time.Duration
import java.time.Duration.ZERO

@RunWith(AlfrescoTestRunner::class)
class MemoryWithAlfrescoPersistencePromenaMutableTransformationManagerExceededBufferSizeTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val transformationExecutionResult = TransformationExecutionResult(
            listOf(
                NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c")
            )
        )
        private val exception = RuntimeException("exception")
    }

    private val serializationService = KryoSerializationService()
    private lateinit var promenaMutableTransformationManager: MemoryWithAlfrescoPersistencePromenaMutableTransformationManager

    @Before
    fun setUp() {
        promenaMutableTransformationManager =
            MemoryWithAlfrescoPersistencePromenaMutableTransformationManager(true, 2, Duration.ofMillis(500), serializationService, serviceRegistry)
    }

    @Test
    fun correctFlowAndWaitingForResultsBeforeTransactionIsCompleted() {
        val transformationExecutions = start4Transformations()
        transformationExecutions.forEach { promenaMutableTransformationManager.completeTransformation(it, transformationExecutionResult) }
        transformationExecutions.forEach { promenaMutableTransformationManager.getResult(it, ZERO) shouldBe transformationExecutionResult }
    }

    @Test
    fun incorrectFlowAndWaitingForResultsBeforeTransactionIsCompleted() {
        val transformationExecutions = start4Transformations()
        transformationExecutions.forEach { promenaMutableTransformationManager.completeErrorTransformation(it, exception) }
        transformationExecutions.forEach {
            shouldThrowExactly<RuntimeException> {
                promenaMutableTransformationManager.getResult(it, ZERO)
            }.message shouldBe exception.message
        }
    }

    private fun start4Transformations(): List<TransformationExecution> =
        (0 until 4).map { promenaMutableTransformationManager.startTransformation() }
}