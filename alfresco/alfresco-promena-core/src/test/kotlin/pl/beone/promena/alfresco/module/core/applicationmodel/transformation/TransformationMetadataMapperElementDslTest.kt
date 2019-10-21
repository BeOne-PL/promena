package pl.beone.promena.alfresco.module.core.applicationmodel.transformation

import io.kotlintest.shouldBe
import org.alfresco.model.ContentModel.PROP_NAME
import org.junit.Test

class TransformationMetadataMapperElementDslTest {

    companion object {
        private const val key = "key"
        private val property = PROP_NAME
        private const val value = 5
    }

    @Test
    fun transformationMetadataMapperElement() {
        transformationMetadataMapperElement(key, property) { it.toString() }.let {
            it.key shouldBe key
            it.property shouldBe property
            it.converter(value) shouldBe value.toString()
        }
    }

    @Test
    fun transformationMetadataMapperElement_defaultConverter() {
        transformationMetadataMapperElement(key, property).let {
            it.key shouldBe key
            it.property shouldBe property
            it.converter(value) shouldBe value
        }
    }
}