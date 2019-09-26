package pl.beone.promena.alfresco.module.connector.activemq.internal

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.service.cmr.repository.NodeRef
import org.junit.Test
import reactor.test.StepVerifier
import java.time.Duration
import kotlin.concurrent.thread

class ReactiveTransformationManagerTest {

    @Test
    fun transform() {
        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeTransformation(id, nodeRefs)
        }

        transformation.block(Duration.ofMillis(500)) shouldBe nodeRefs
    }

    @Test
    fun `transform _ should throw RuntimeException`() {
        val id = "1"
        val uuid = "f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeErrorTransformation(id, RuntimeException(uuid))
        }

        shouldThrow<RuntimeException> {
            transformation.block(Duration.ofMillis(500))
        }.message shouldBe uuid
    }

    @Test
    fun `transform _ block time exceeded _ should throw IllegalStateException`() {
        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeTransformation(id, nodeRefs)
        }

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofMillis(100))
        }
    }

    @Test
    fun transformAsync() {
        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeTransformation(id, nodeRefs)
        }

        StepVerifier.create(transformation)
            .expectNext(nodeRefs)
            .expectComplete()
            .verify()

        StepVerifier.create(transformation)
            .expectNext(nodeRefs)
            .expectComplete()
            .verify()
    }

    @Test
    fun `transformAsync _ started the same transaction two times`() {
        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        val reactiveTransformationManager = ReactiveTransformationManager()
        reactiveTransformationManager.startTransformation(id)
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeTransformation(id, nodeRefs)
        }

        StepVerifier.create(transformation)
            .expectNext(nodeRefs)
            .expectComplete()
            .verify()
    }

    @Test
    fun `transformAsync _ should throw RuntimeException`() {
        val id = "1"
        val uuid = "f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)
        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeErrorTransformation(id, RuntimeException(uuid))
        }

        StepVerifier.create(transformation)
            .expectErrorSatisfies {
                it should beInstanceOf<RuntimeException>()
                it.message shouldBe "f0ee3818-9cc3-4e4d-b20b-1b5d8820e133"
            }
            .verify()
    }

    @Test
    fun `transform and transformAsync _ shouldn't block cancel mono`() {
        val id = "1"
        val nodeRefs = listOf(NodeRef("workspace://SpacesStore/68462d80-70d4-4b02-bda2-be5660b2413e"))

        val reactiveTransformationManager = ReactiveTransformationManager()
        val transformation = reactiveTransformationManager.startTransformation(id)

        thread {
            Thread.sleep(300)
            reactiveTransformationManager.completeTransformation(id, nodeRefs)
        }

        shouldThrow<IllegalStateException> {
            transformation.block(Duration.ofMillis(100))
        }

        StepVerifier.create(transformation)
            .expectNext(nodeRefs)
            .expectComplete()
            .verify()
    }
}