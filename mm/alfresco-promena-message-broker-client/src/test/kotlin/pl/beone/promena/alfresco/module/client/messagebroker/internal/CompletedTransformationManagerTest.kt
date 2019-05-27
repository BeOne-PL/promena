package pl.beone.promena.alfresco.module.client.messagebroker.internal

import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.collections.shouldHaveSingleElement
import io.kotlintest.matchers.withClue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.alfresco.service.cmr.repository.NodeRef
import org.apache.commons.lang3.exception.ExceptionUtils
import java.time.Duration
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeoutException
import kotlin.concurrent.thread

data class IdWithNodeRefs(val id: String,
                          val nodeRefs: List<NodeRef>)

class CompletedTransformationManagerTest : StringSpec() {

    init {
        "should wait for completion of all transformation" {
            val exceptionList = CopyOnWriteArrayList<Exception>()
            val completedTransformationManager = CompletedTransformationManager()
            val idWithNodeRef = (0..100)
                    .map { IdWithNodeRefs(it.toString(), listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}"))) }

            idWithNodeRef.forEach { (id, _) -> completedTransformationManager.startTransformation(id) }
            listOf(
                    thread {
                        try {
                            idWithNodeRef.forEach { (id, nodeRefs) ->
                                completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(2000)) shouldBe nodeRefs
                            }
                        } catch (e: Exception) {
                            exceptionList.add(e)
                        }
                    },
                    thread {
                        Thread.sleep(200)

                        try {
                            idWithNodeRef.forEach { (id, nodeRefs) ->
                                completedTransformationManager.completeTransformation(id, nodeRefs)
                            }
                        } catch (e: Exception) {
                            exceptionList.add(e)
                        }
                    }
            ).forEach { it.join() }

            withClue("Something went wrong. Check exception: ${toArrayOfString(exceptionList)}") { exceptionList.shouldBeEmpty() }
        }

        "should exceed waiting time and throw TimeoutException" {
            val exceptionList = CopyOnWriteArrayList<Exception>()
            val completedTransformationManager = CompletedTransformationManager()

            val id = "1"
            val nodeRefs = listOf(NodeRef("workspace://SpacesStore/${UUID.randomUUID()}"))

            completedTransformationManager.startTransformation(id)
            listOf(
                    thread {
                        try {
                            completedTransformationManager.getTransformedNodeRefs(id, Duration.ofMillis(100)) shouldBe nodeRefs
                        } catch (e: Exception) {
                            exceptionList.add(e)
                        }
                    },
                    thread {
                        Thread.sleep(200)

                        try {
                            completedTransformationManager.completeTransformation(id, nodeRefs)
                        } catch (e: Exception) {
                            exceptionList.add(e)
                        }
                    }
            ).forEach { it.join() }

            withClue("Something went wrong. List shouldn't be empty and should contain only TimeoutException exceptions: : " +
                     "${toArrayOfString(exceptionList)}") {
                exceptionList.map { it.javaClass.name } shouldHaveSingleElement TimeoutException::class.java.name
            }

        }
    }

    private fun toArrayOfString(exceptionList: CopyOnWriteArrayList<Exception>): List<String> =
            exceptionList.map { ExceptionUtils.getStackTrace(it) }
}