package pl.beone.promena.transformer.contract.transformation

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.mockk.mockk
import org.junit.jupiter.api.Test
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML
import pl.beone.promena.transformer.contract.model.Parameters
import pl.beone.promena.transformer.contract.transformer.toTransformerId

class TransformationDslTest {

    companion object {
        private const val transformerName = "test"
        private val targetMediaType = TEXT_PLAIN
        private val parameters = mockk<Parameters>()

        private const val transformerName2 = "test2"
        private val targetMediaType2 = TEXT_XML
        private val parameters2 = mockk<Parameters>()

        private const val transformerSubName = "sub-test"

        private val singleTransformation = Transformation.Single.of(transformerName, targetMediaType, parameters)
        private val singleTransformationWithSubName = Transformation.Single.of(transformerName, transformerSubName, targetMediaType, parameters)
        private val singleTransformation2 = Transformation.Single.of(transformerName2, targetMediaType2, parameters2)
    }

    @Test
    fun singleTransformation() {
        singleTransformation(transformerName, targetMediaType, parameters) shouldBe
                singleTransformation
    }

    @Test
    fun `singleTransformation _ transformation id`() {
        singleTransformation((transformerName to transformerSubName).toTransformerId(), targetMediaType, parameters) shouldBe
                singleTransformationWithSubName
    }

    @Test
    fun `singleTransformation _ with null sub name`() {
        singleTransformation(transformerName, transformerSubName, targetMediaType, parameters) shouldBe
                singleTransformationWithSubName
    }

    @Test
    fun `next _ single transformation`() {
        singleTransformation(transformerName, targetMediaType, parameters) next
                singleTransformation(transformerName2, targetMediaType2, parameters2) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun compositeTransformation() {
        compositeTransformation(singleTransformation, listOf(singleTransformation2)) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2))

        compositeTransformation(singleTransformation, singleTransformation2) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun `next _ composite transformation`() {
        compositeTransformation(
            singleTransformation(transformerName, targetMediaType, parameters),
            singleTransformation(transformerName2, targetMediaType2, parameters2)
        ) next singleTransformation(transformerName, targetMediaType, parameters) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2, singleTransformation))
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
                Transformation.Single.of(transformerName, targetMediaType, parameters)
    }

    @Test
    fun `transformation _ two single transformations - Composite`() {
        transformation(listOf(singleTransformation, singleTransformation2)) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2))

        transformation(singleTransformation, singleTransformation2) shouldBe
                Transformation.Composite.of(listOf(singleTransformation, singleTransformation2))
    }

    @Test
    fun toTransformation() {
        listOf(singleTransformation).toTransformation() shouldBe
                Transformation.Single.of(transformerName, targetMediaType, parameters)
    }

}