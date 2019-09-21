package pl.beone.promena.alfresco.module.rendition.configuration.internal

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionDefinitionValidationException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter

class AlfrescoPromenaRenditionDefinitionValidatorTest {

    @Test
    fun validateUniqueDefinitions() {
        val doclib = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val pdf = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }

        val alfrescoPromenaRenditionDefinitionGetter = mockk<AlfrescoPromenaRenditionDefinitionGetter> {
            every { getAll() } returns listOf(doclib, pdf)
        }

        shouldNotThrow<AlfrescoPromenaRenditionDefinitionValidationException> {
            AlfrescoPromenaRenditionDefinitionValidator(alfrescoPromenaRenditionDefinitionGetter)
                .validateUniqueDefinitions()
        }
    }

    @Test
    fun `validateUniqueDefinitions _ doubled definitions _ should throw AlfrescoPromenaRenditionDefinitionValidationException`() {
        val doclib = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val doclib2 = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val pdf = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }
        val pdf2 = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }
        val avatar = mockk<AlfrescoPromenaRenditionDefinition> {
            every { getRenditionName() } returns "avatar"
        }

        val alfrescoPromenaRenditionDefinitionGetter = mockk<AlfrescoPromenaRenditionDefinitionGetter> {
            every { getAll() } returns listOf(doclib, doclib2, pdf, pdf2, avatar)
        }

        shouldThrow<AlfrescoPromenaRenditionDefinitionValidationException> {
            AlfrescoPromenaRenditionDefinitionValidator(alfrescoPromenaRenditionDefinitionGetter)
                .validateUniqueDefinitions()
        }.let {
            it.renditionNameToNotUniqueDefinitionsMap shouldBe
                    mapOf(
                        "doclib" to listOf(doclib, doclib2),
                        "pdf" to listOf(pdf, pdf2)
                    )

            it.message!!.split("\n").let { messages ->
                messages[0] shouldBe "Detected <2> definitions with duplicated rendition name:"
                messages[1] shouldStartWith "> doclib: "
                messages[2] shouldStartWith "> pdf: "
            }
        }
    }
}