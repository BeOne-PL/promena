package pl.beone.promena.alfresco.module.core.external.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrowExactly
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.alfresco.rad.test.AlfrescoTestRunner
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef
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
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

@RunWith(AlfrescoTestRunner::class)
class MemoryWithAlfrescoPersistencePromenaMutableTransformationManagerTestIT : AbstractUtilsAlfrescoIT() {

    companion object {
        private val transformationExecutionResult = TransformationExecutionResult(
            listOf(
                NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f"),
                NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c")
            )
        )
        private val exception = RuntimeException("exception")
    }

    private val serializationService = KryoSerializationService()
    private lateinit var promenaMutableTransformationManager: MemoryWithAlfrescoPersistencePromenaMutableTransformationManager

    private val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    @Before
    fun setUp() {
        promenaMutableTransformationManager =
            MemoryWithAlfrescoPersistencePromenaMutableTransformationManager(true, 100, Duration.ofMillis(500), serializationService, serviceRegistry)
    }

    @Test
    fun correctFlowAndWaitingForResultBeforeTransactionIsCompleted() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
        promenaMutableTransformationManager.getResult(transformationExecution, ZERO) shouldBe transformationExecutionResult
    }

    @Test
    fun correctFlowAndWaitingForResultAfterTransactionIsCompleted() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
            }

            launch {
                delay(500)
                promenaMutableTransformationManager.getResult(transformationExecution, ZERO) shouldBe transformationExecutionResult
            }
        }
    }

    @Test
    fun incorrectFlowAndWaitingForResultBeforeTransactionIsCompleted() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, exception)
        shouldThrowExactly<RuntimeException> {
            promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
        }.message shouldBe exception.message
    }

    @Test
    fun incorrectFlowAndWaitingForResultAfterTransactionIsCompleted() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                promenaMutableTransformationManager.completeErrorTransformation(transformationExecution, exception)
            }

            launch {
                delay(500)
                shouldThrowExactly<RuntimeException> {
                    promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
                }.message shouldBe exception.message

            }
        }
    }

    @Test
    fun gettingResultBeforeTransactionIsCompleted_shouldThrowTimeoutException() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                delay(1000)
                promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
            }

            launch {
                delay(500)
                shouldThrowExactly<TimeoutException> {
                    promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
                }.message shouldBe "Waiting time for transformation <${transformationExecution.id}> has expired"
            }

            launch {
                delay(2000)
                promenaMutableTransformationManager.getResult(transformationExecution) shouldBe transformationExecutionResult
            }
        }
    }

    @Test
    fun defaultWaitMax() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                delay(700)
                promenaMutableTransformationManager.completeTransformation(transformationExecution, transformationExecutionResult)
            }

            launch {
                shouldThrowExactly<TimeoutException> {
                    promenaMutableTransformationManager.getResult(transformationExecution)
                }.message shouldBe "Waiting time for transformation <${transformationExecution.id}> has expired"
            }

            launch {
                delay(1100)
                promenaMutableTransformationManager.getResult(transformationExecution) shouldBe transformationExecutionResult
            }
        }
    }

    @Test
    fun gettingResultBeforeTransactionIsStarted_shouldThrowIllegalStateException() {
        shouldThrowExactly<IllegalStateException> {
            promenaMutableTransformationManager.getResult(TransformationExecution("absent"))
        }.message shouldBe "There is no transformation <absent> in progress"
    }

    @Test
    fun markingTransactionThatWasNotStartedAsComplete_shouldNotThrowAnyException() {
        shouldNotThrowAny {
            promenaMutableTransformationManager.completeTransformation(TransformationExecution("absent"), transformationExecutionResult)
        }
    }

    @Test
    fun stressTest() {
        val transformationExecution = (1..100).map { promenaMutableTransformationManager.startTransformation() }
        runBlocking(dispatcher) {
            transformationExecution.forEach {
                launch {
                    promenaMutableTransformationManager.completeTransformation(it, transformationExecutionResult)
                }
            }

            transformationExecution.forEach {
                launch {
                    promenaMutableTransformationManager.getResult(it, Duration.ofMillis(3000)) shouldBe transformationExecutionResult
                }
            }
        }
    }
}