package pl.beone.promena.alfresco.module.client.messagebroker.internal

import org.alfresco.service.cmr.repository.NodeRef
import org.apache.tika.utils.ExceptionUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

data class IdWithNodeRefs(val id: String,
                          val nodeRefs: List<NodeRef>)

class CompletedTransformationManagerTest {

    @Test
    fun `all _ should wait for completion of all transformation`() {
        val exceptionList = CopyOnWriteArrayList<Exception>()

        val completedTransformationManager = CompletedTransformationManager()

        val idWithNodeRef = (0..100)
                .map { IdWithNodeRefs(it.toString(), listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}"))) }

        //
        idWithNodeRef.forEach { (id, _) -> completedTransformationManager.startTransformation(id) }

        val consumerThread = thread {
            try {
                idWithNodeRef.forEach { (id, nodeRefs) ->
                    assertThat(completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(2000))).isEqualTo(nodeRefs)
                }
            } catch (e: Exception) {
                exceptionList.add(e)
            }
        }

        val producerThread = thread {
            Thread.sleep(200)

            try {
                idWithNodeRef.forEach { (id, nodeRefs) ->
                    completedTransformationManager.completeTransformation(id, nodeRefs)
                }
            } catch (e: Exception) {
                exceptionList.add(e)
            }
        }

        consumerThread.join()
        producerThread.join()

        //
        assertThat(exceptionList)
                .`as`("Something went wrong. Check exception: %s", toArrayOfString(exceptionList))
                .isEmpty()
    }

    @Test
    fun `all _ should throw TimeoutException`() {
        val exceptionList = CopyOnWriteArrayList<Exception>()

        val completedTransformationManager = CompletedTransformationManager()

        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}"))

        //
        completedTransformationManager.startTransformation(id)

        val consumerThread = thread {
            try {
                assertThat(completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(100))).isEqualTo(nodeRefs)
            } catch (e: Exception) {
                exceptionList.add(e)
            }
        }

        val producerThread = thread {
            Thread.sleep(200)

            try {
                completedTransformationManager.completeTransformation(id, nodeRefs)
            } catch (e: Exception) {
                exceptionList.add(e)
            }
        }

        consumerThread.join()
        producerThread.join()

        //
        assertThat(exceptionList.map { it.javaClass.name })
                .`as`("Something went wrong. List shouldn't be empty and should contain only TimeoutException exceptions: %s", toArrayOfString(exceptionList))
                .containsOnly(TimeoutException::class.java.name)
                .isNotEmpty
    }


    private fun toArrayOfString(exceptionList: CopyOnWriteArrayList<Exception>): List<String> =
            exceptionList.map { ExceptionUtils.getStackTrace(it) }
}