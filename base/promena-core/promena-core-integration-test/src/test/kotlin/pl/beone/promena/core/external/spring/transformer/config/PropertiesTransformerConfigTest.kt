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
import pl.beone.promena.transformer.contract.data.DataDescriptor
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor
import pl.beone.promena.transformer.contract.model.Parameters

private abstract class AbstractTransformer : Transformer {

    override fun transform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): TransformedDataDescriptor =
            throw Exception("No matter")

    override fun canTransform(dataDescriptor: DataDescriptor, targetMediaType: MediaType, parameters: Parameters): Boolean =
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
        propertiesTransformerConfig.getId(TransformerWithProperties()) shouldBe "transformerWithProperties"
    }

    @Test
    fun getActors() {
        propertiesTransformerConfig.getActors(TransformerWithProperties()) shouldBe 3
    }

    @Test
    fun getPriority() {
        propertiesTransformerConfig.getPriority(TransformerWithProperties()) shouldBe 2
    }

    @Test
    fun `getTransformationId _ no property _ should throw IllegalStateException`() {
        shouldThrow<IllegalStateException> { propertiesTransformerConfig.getId(TransformerWithoutProperties()) }
                .message shouldBe "There is no <transformer.pl.beone.promena.core.external.spring.transformer.config.TransformerWithoutProperties.transformationId> property. Transformer must have <transformerId>"
    }

    @Test
    fun `getActors _ no property _ should use default 1`() {
        propertiesTransformerConfig.getActors(TransformerWithoutProperties()) shouldBe 1
    }

    @Test
    fun `getPriority _ no property _ should use default 0`() {
        propertiesTransformerConfig.getPriority(TransformerWithoutProperties()) shouldBe 0
    }

}