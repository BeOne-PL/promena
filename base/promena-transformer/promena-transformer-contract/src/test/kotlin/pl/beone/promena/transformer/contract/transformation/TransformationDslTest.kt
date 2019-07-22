package pl.beone.promena.transformer.contract.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.model.Parameters

class TransformationDslTest {

    companion object {
        private const val id = "test"
        private val targetMediaType = TEXT_PLAIN
        private val parameters = mockk<Parameters>()

        private const val id2 = "test2"
        private val targetMediaType2 = TEXT_XML
        private val parameters2 = mockk<Parameters>()

        private val singleTransformation = Transformation.Single(id, targetMediaType, parameters)
        private val singleTransformation2 = Transformation.Single(id2, targetMediaType2, parameters2)
    }

    @Test
    fun singleTransformation() {
        singleTransformation(id, targetMediaType, parameters) shouldBe
                singleTransformation
    }

    @Test
    fun `next _ single transformation`() {
        singleTransformation(id, targetMediaType, parameters) next singleTransformation(id2, targetMediaType2, parameters2) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun compositeTransformation() {
        compositeTransformation(singleTransformation, listOf(singleTransformation2)) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2))

        compositeTransformation(singleTransformation, singleTransformation2) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun `next _ composite transformation`() {
        compositeTransformation(singleTransformation(id, targetMediaType, parameters), singleTransformation(id2, targetMediaType2, parameters2)) next
                singleTransformation(id, targetMediaType, parameters) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2, singleTransformation))
    }

    @Test
    fun `transformation _ zero transformation _ should throw IllegalArgumentException`() {
        shouldThrow<IllegalArgumentException> {
            transformation()
        }.message shouldBe "Transformation must consist of at least one transformer"
    }

    @Test
    fun `transformation _ one single transformation - Single`() {
        transformation(singleTransformation) shouldBe
                Transformation.Single(id, targetMediaType, parameters)
    }

    @Test
    fun `transformation _ two single transformations - Composite`() {
        transformation(listOf(singleTransformation, singleTransformation2)) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2))

        transformation(singleTransformation, singleTransformation2) shouldBe
                Transformation.Composite(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun toTransformation() {
        listOf(singleTransformation).toTransformation() shouldBe
                Transformation.Single(id, targetMediaType, parameters)
    }

}