package pl.beone.promena.alfresco.module.rendition.configuration.internal

import io.kotlintest.matchers.string.shouldStartWith
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrow
import io.kotlintest.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.alfresco.module.rendition.contract.definition.PromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.definition.PromenaRenditionDefinitionGetter

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

        with(shouldThrow<IllegalStateException> {
            PromenaRenditionDefinitionValidator(promenaRenditionDefinitionGetter)
                .validateUniqueDefinitions()
        }) {
            with(message!!.split("\n")) {
                this[0] shouldBe "Detected <2> definitions with duplicated rendition name:"
                this[1] shouldStartWith "> doclib: "
                this[2] shouldStartWith "> pdf: "
            }
        }
    }
}