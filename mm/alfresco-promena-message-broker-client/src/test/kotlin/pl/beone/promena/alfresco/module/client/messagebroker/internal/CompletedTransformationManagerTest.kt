package pl.beone.promena.alfresco.module.client.messagebroker.internal

import io.kotlintest.inspectors.forAll
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.commons.lang3.exception.ExceptionUtils
import org.junit.Test
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

data class SimpleException(val uuid: String) : RuntimeException()

data class IdWithResult<T>(val id: String,
                           val result: T)

class CompletedTransformationManagerTest {

    @Test
    fun `should wait for completion of all transformation`() {
        val exceptions = CopyOnWriteArrayList<Throwable>()

        val completedTransformationManager = CompletedTransformationManager()
        val idWithNodeRefs = (0..50)
                .map { IdWithResult(it.toString(), listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}"))) }
        val idWithException = (51..100)
                .map { IdWithResult(it.toString(), SimpleException(UUID.randomUUID().toString())) }

        (idWithNodeRefs + idWithException).map { it.id }
                .forEach { completedTransformationManager.startTransformation(it) }
        listOf(
                thread {
                    try {
                        idWithNodeRefs.forEach { (id, nodeRefs) ->
                            completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(2000)) shouldBe nodeRefs
                        }
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                },
                thread {
                    idWithException.forEach { (id, exception) ->
                        try {
                            shouldThrow<SimpleException> {
                                completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(2000))
                            }
                                    .uuid shouldBe exception.uuid
                        } catch (e: AssertionError) {
                            exceptions.add(e)
                        }
                    }
                },
                thread {
                    Thread.sleep(200)

                    try {
                        idWithNodeRefs.forEach { (id, nodeRefs) ->
                            completedTransformationManager.completeTransformation(id, nodeRefs)
                        }
                        idWithException.forEach { (id, exception) ->
                            completedTransformationManager.completeErrorTransformation(id, exception)
                        }
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                }
        ).forEach { it.join() }

        withClue("Something went wrong. Check exception: ${toArrayOfString(exceptions)}") { exceptions.shouldBeEmpty() }
    }

    @Test
    fun `should exceed waiting time and throw TimeoutException`() {
        val exceptions = CopyOnWriteArrayList<Throwable>()
        val completedTransformationManager = CompletedTransformationManager()

        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/98c8a344-7724-473d-9dd2-c7c29b77a0ff"))
        val exception = SimpleException("2")

        completedTransformationManager.startTransformation("1")
        completedTransformationManager.startTransformation("2")
        listOf(
                thread {
                    try {
                        completedTransformationManager.getTransformedNodeRefs("1", Duration.ofMillis(100)) shouldBe nodeRefs
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                },
                thread {
                    try {
                        completedTransformationManager.getTransformedNodeRefs("2", Duration.ofMillis(100)) shouldBe exception
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                },
                thread {
                    Thread.sleep(500)

                    try {
                        completedTransformationManager.completeTransformation("1", nodeRefs)
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                },
                thread {
                    Thread.sleep(500)

                    try {
                        completedTransformationManager.completeErrorTransformation("2", exception)
                    } catch (e: Exception) {
                        exceptions.add(e)
                    }
                }
        ).forEach { it.join() }

        withClue("Something went wrong. List shouldn't be empty and should contain only TimeoutException exceptions: : " +
                 "${toArrayOfString(exceptions)}") {
            exceptions.map { it.javaClass.name }.forAll { it shouldBe TimeoutException::class.java.name }
        }
    }

    private fun toArrayOfString(exceptionList: CopyOnWriteArrayList<Throwable>): List<String> =
            exceptionList.map { ExceptionUtils.getStackTrace(it) }
}