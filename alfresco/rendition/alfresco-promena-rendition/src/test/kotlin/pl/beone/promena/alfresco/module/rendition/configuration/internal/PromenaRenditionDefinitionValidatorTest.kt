package pl.beone.promena.alfresco.module.rendition.configuration.internal

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionGetter

class PromenaRenditionDefinitionValidatorTest {

    @Test
    fun validateUniqueDefinitions() {
        val doclib = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val pdf = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }

        val promenaRenditionDefinitionGetter = mockk<PromenaRenditionDefinitionGetter> {
            every { getAll() } returns listOf(doclib, pdf)
        }

        shouldNotThrow<IllegalStateException> {
            PromenaRenditionDefinitionValidator(promenaRenditionDefinitionGetter)
                .validateUniqueDefinitions()
        }
    }

    @Test
    fun `validateUniqueDefinitions _ doubled definitions _ should throw IllegalStateException`() {
        val doclib = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val doclib2 = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "doclib"
        }
        val pdf = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }
        val pdf2 = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "pdf"
        }
        val avatar = mockk<PromenaRenditionDefinition> {
            every { getRenditionName() } returns "avatar"
        }

        val promenaRenditionDefinitionGetter = mockk<PromenaRenditionDefinitionGetter> {
            every { getAll() } returns listOf(doclib, doclib2, pdf, pdf2, avatar)
        }

        shouldThrow<IllegalStateException> {
            PromenaRenditionDefinitionValidator(promenaRenditionDefinitionGetter)
                .validateUniqueDefinitions()
        }.let {
            it.message!!.split("\n").let { messages ->
                messages[0] shouldBe "Detected <2> definitions with duplicated rendition name:"
                messages[1] shouldStartWith "> doclib: "
                messages[2] shouldStartWith "> pdf: "
            }
        }
    }
}