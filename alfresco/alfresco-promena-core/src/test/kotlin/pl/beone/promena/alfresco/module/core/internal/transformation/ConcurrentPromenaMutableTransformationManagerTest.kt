package pl.beone.promena.alfresco.module.core.internal.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.kotlintest.shouldThrowExactly
import io.mockk.mockk
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecution
import pl.beone.promena.alfresco.module.core.applicationmodel.transformation.TransformationExecutionResult
import java.time.Duration
import java.time.Duration.ZERO
import java.util.concurrent.Executors
import java.util.concurrent.TimeoutException

class ConcurrentPromenaMutableTransformationManagerTest {

    companion object {
        private val transformationExecutionResult = TransformationExecutionResult(listOf(mockk()))
        private val exception = RuntimeException("exception")
    }

    private val dispatcher = Executors.newFixedThreadPool(4).asCoroutineDispatcher()
    private lateinit var promenaMutableTransformationManager: ConcurrentPromenaMutableTransformationManager

    @Before
    fun setUp() {
        promenaMutableTransformationManager =
            ConcurrentPromenaMutableTransformationManager(
                100,
                Duration.ofMillis(500)
            )
    }

    @Test
    fun `correct flow and waiting for result before transaction is completed`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        promenaMutableTransformationManager.completeTransformation(
            transformationExecution,
            transformationExecutionResult
        )
        promenaMutableTransformationManager.getResult(transformationExecution, ZERO) shouldBe transformationExecutionResult
    }

    @Test
    fun `correct flow and waiting for result after transaction is completed`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                promenaMutableTransformationManager.completeTransformation(
                    transformationExecution,
                    transformationExecutionResult
                )
            }

            launch {
                delay(500)
                promenaMutableTransformationManager.getResult(transformationExecution, ZERO) shouldBe transformationExecutionResult
            }
        }
    }

    @Test
    fun `incorrect flow and waiting for result before transaction is completed`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        promenaMutableTransformationManager.completeErrorTransformation(
            transformationExecution,
            exception
        )
        shouldThrowExactly<RuntimeException> {
            promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
        }.message shouldBe "exception"
    }

    @Test
    fun `incorrect flow and waiting for result after transaction is completed`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                promenaMutableTransformationManager.completeErrorTransformation(
                    transformationExecution,
                    exception
                )
            }

            launch {
                delay(500)
                shouldThrowExactly<RuntimeException> {
                    promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
                }.message shouldBe "exception"

            }
        }
    }

    @Test
    fun `getting result before transaction is completed _ should throw TimeoutException`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                delay(1000)
                promenaMutableTransformationManager.completeTransformation(
                    transformationExecution,
                    transformationExecutionResult
                )
            }

            launch {
                delay(500)
                shouldThrowExactly<TimeoutException> {
                    promenaMutableTransformationManager.getResult(transformationExecution, ZERO)
                }.message shouldBe "Waiting time for transformation <${transformationExecution.id}> has expired"
            }

            launch {
                delay(1500)
                promenaMutableTransformationManager.getResult(transformationExecution) shouldBe transformationExecutionResult
            }
        }
    }

    @Test
    fun `default waitMax`() {
        val transformationExecution = promenaMutableTransformationManager.startTransformation()
        runBlocking(dispatcher) {
            launch {
                delay(700)
                promenaMutableTransformationManager.completeTransformation(
                    transformationExecution,
                    transformationExecutionResult
                )
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
    fun `getting result before transaction is started _ should throw IllegalStateException`() {
        shouldThrowExactly<IllegalStateException> {
            promenaMutableTransformationManager.getResult(TransformationExecution("absent"))
        }.message shouldBe "There is no transformation <absent> in progress"
    }

    @Test
    fun `marking transaction that wasn't started as complete _ shouldn't throw any exception`() {
        shouldNotThrowAny {
            promenaMutableTransformationManager.completeTransformation(
                TransformationExecution("absent"),
                transformationExecutionResult
            )
        }
    }

    @Test
    fun `stress test`() {
        val transformationExecution = (1..100).map { promenaMutableTransformationManager.startTransformation() }
        runBlocking(dispatcher) {
            transformationExecution.forEach {
                launch {
                    promenaMutableTransformationManager.completeTransformation(
                        it,
                        transformationExecutionResult
                    )
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