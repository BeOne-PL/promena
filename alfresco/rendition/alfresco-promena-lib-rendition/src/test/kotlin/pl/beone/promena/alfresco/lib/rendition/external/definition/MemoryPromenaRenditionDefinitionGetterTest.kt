package pl.beone.promena.alfresco.lib.rendition.external.definition

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.lib.rendition.applicationmodel.exception.NoSuchPromenaRenditionDefinitionException
import pl.beone.promena.alfresco.lib.rendition.contract.definition.PromenaRenditionDefinition

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
        promenaRenditionDefinitionGetter.getByRenditionName("doclib") shouldBe doclib
    }

    @Test
    fun `getByRenditionName _ absent rendition _ should throw NoSuchPromenaRenditionDefinitionException`() {
        with(shouldThrow<NoSuchPromenaRenditionDefinitionException> {
            promenaRenditionDefinitionGetter.getByRenditionName("absent")
        }) {
            renditionName shouldBe "absent"
            promenaRenditionDefinitions shouldBe definitions
            message shouldStartWith "There is no <absent> Promena rendition definition. Available renditions:"
        }
    }
}