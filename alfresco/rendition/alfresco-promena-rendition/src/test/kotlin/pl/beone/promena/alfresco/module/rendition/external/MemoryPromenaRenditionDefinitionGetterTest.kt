package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.NoSuchPromenaRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinition

class MemoryPromenaRenditionDefinitionGetterTest {

    private lateinit var promenaRenditionDefinitionGetter: MemoryPromenaRenditionDefinitionGetter

    private lateinit var doclib: PromenaRenditionDefinition
    private lateinit var pdf: PromenaRenditionDefinition

    private lateinit var definitions: List<PromenaRenditionDefinition>

    @Before
    fun setUp() {
        doclib = mockk {
            every { getRenditionName() } returns "doclib"
        }
        pdf = mockk {
            every { getRenditionName() } returns "pdf"
        }

        definitions = listOf(doclib, pdf)

        promenaRenditionDefinitionGetter = MemoryPromenaRenditionDefinitionGetter(definitions)
    }

    @Test
    fun getAll() {
        promenaRenditionDefinitionGetter.getAll() shouldBe definitions
    }

    @Test
    fun getByRenditionName() {
        promenaRenditionDefinitionGetter.getByRenditionName("doclib") shouldBe
                doclib
    }

    @Test
    fun `getByRenditionName _ absent rendition _ should throw NoSuchPromenaRenditionDefinitionException`() {
        shouldThrow<NoSuchPromenaRenditionDefinitionException> {
            promenaRenditionDefinitionGetter.getByRenditionName("absent")
        }.let {
            it.renditionName shouldBe "absent"
            it.promenaRenditionDefinitions shouldBe definitions
            it.message shouldStartWith "There is no <absent> Promena rendition definition. Available renditions:"
        }
    }
}