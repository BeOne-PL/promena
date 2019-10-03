package pl.beone.promena.alfresco.module.rendition.external

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.NoSuchAlfrescoPromenaRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition

class MemoryAlfrescoPromenaRenditionDefinitionGetterTest {

    private lateinit var alfrescoPromenaRenditionDefinitionGetter: MemoryAlfrescoPromenaRenditionDefinitionGetter

    private lateinit var doclib: AlfrescoPromenaRenditionDefinition
    private lateinit var pdf: AlfrescoPromenaRenditionDefinition

    private lateinit var definitions: List<AlfrescoPromenaRenditionDefinition>

    @Before
    fun setUp() {
        doclib = mockk {
            every { getRenditionName() } returns "doclib"
        }
        pdf = mockk {
            every { getRenditionName() } returns "pdf"
        }

        definitions = listOf(doclib, pdf)

        alfrescoPromenaRenditionDefinitionGetter = MemoryAlfrescoPromenaRenditionDefinitionGetter(definitions)
    }

    @Test
    fun getAll() {
        alfrescoPromenaRenditionDefinitionGetter.getAll() shouldBe definitions
    }

    @Test
    fun getByRenditionName() {
        alfrescoPromenaRenditionDefinitionGetter.getByRenditionName("doclib") shouldBe
                doclib
    }

    @Test
    fun `getByRenditionName _ absent rendition _ should throw NoSuchAlfrescoPromenaRenditionDefinitionException`() {
        shouldThrow<NoSuchAlfrescoPromenaRenditionDefinitionException> {
            alfrescoPromenaRenditionDefinitionGetter.getByRenditionName("absent")
        }.let {
            it.renditionName shouldBe "absent"
            it.alfrescoPromenaRenditionDefinitions shouldBe definitions
            it.message shouldStartWith "There is no <absent> Promena rendition definition. Available renditions:"
        }
    }
}