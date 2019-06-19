package pl.beone.promena.core.external.spring.transformer.config

import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType
import pl.beone.promena.transformer.contract.Transformer
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

private abstract class AbstractTransformer : Transformer {
    override fun transform(dataDescriptors: List<DataDescriptor>,
                           targetMediaType: MediaType,
                           parameters: Parameters): List<TransformedDataDescriptor> =
            throw Exception("No matter")

    override fun canTransform(dataDescriptors: List<DataDescriptor>, targetMediaType: MediaType, parameters: Parameters): Boolean =
            throw Exception("No matter")
}

private class TransformerWithProperties : AbstractTransformer()
private class TransformerWithoutProperties : AbstractTransformer()

@RunWith(SpringRunner::class)
@SpringBootTest(classes = [
    PropertiesTransformerConfig::class
], properties = [
    "transformer.pl.beone.promena.core.external.spring.transformer.config.TransformerWithProperties.transformationId=transformerWithProperties",
    "transformer.pl.beone.promena.core.external.spring.transformer.config.TransformerWithProperties.actors=3",
    "transformer.pl.beone.promena.core.external.spring.transformer.config.TransformerWithProperties.priority=2"
])
class PropertiesTransformerConfigTest {

    @Autowired
    private lateinit var propertiesTransformerConfig: PropertiesTransformerConfig

    @Test
    fun getTransformationId() {
        TransformerWithProperties().let {
            propertiesTransformerConfig.getTransformationId(it) shouldBe "transformerWithProperties"
        }
    }

    @Test
    fun getActors() {
        TransformerWithProperties().let {
            propertiesTransformerConfig.getActors(it) shouldBe 3
        }
    }

    @Test
    fun getPriority() {
        TransformerWithProperties().let {
            propertiesTransformerConfig.getPriority(it) shouldBe 2
        }
    }

    @Test
    fun `getTransformationId _ no property _ should throw IllegalStateException`() {
        TransformerWithoutProperties().let {
            shouldThrow<IllegalStateException> { propertiesTransformerConfig.getTransformationId(it) }
                    .message shouldBe "There is no <transformer.pl.beone.promena.core.external.spring.transformer.config.TransformerWithoutProperties.transformationId> property. Transformer must have <transformerId>"
        }
    }

    @Test
    fun `getActors _ no property _ should use default 1`() {
        TransformerWithoutProperties().let {
            propertiesTransformerConfig.getActors(it) shouldBe 1
        }
    }

    @Test
    fun `getPriority _ no property _ should use default 0`() {
        TransformerWithoutProperties().let {
            propertiesTransformerConfig.getPriority(it) shouldBe 0
        }
    }

}