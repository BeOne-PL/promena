package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.cmr.repository.StoreRef.STORE_REF_WORKSPACE_SPACESSTORE
import org.junit.Test

class TransformationExecutionResultDslTest {

    companion object {
        private val nodeRef = NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "7abdf1e2-92f4-47b2-983a-611e42f3555c")
        private val nodeRef2 = NodeRef(STORE_REF_WORKSPACE_SPACESSTORE, "b0bfb14c-be38-48be-90c3-cae4a7fd0c8f")
        private val nodeRefs = listOf(nodeRef, nodeRef2)
    }

    @Test
    fun transformationExecutionResult_list() {
        transformationExecutionResult(nodeRefs).nodeRefs shouldBe nodeRefs
    }

    @Test
    fun transformationExecutionResult_vararg() {
        transformationExecutionResult(nodeRef, nodeRef2).nodeRefs shouldBe nodeRefs
    }
}