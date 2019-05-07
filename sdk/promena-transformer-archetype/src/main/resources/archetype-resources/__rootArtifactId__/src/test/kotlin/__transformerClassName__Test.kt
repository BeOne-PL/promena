package ${package}

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.lib.dockertestrunner.external.DockerTestRunner
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor
import pl.beone.promena.transformer.internal.model.data.InMemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

@RunWith(DockerTestRunner::class)
class ${transformerClassName}Test {

    private companion object {
        private val transformer = ${transformerClassName}()
    }

    @Test
    fun transform() {
        transformer.transform(listOf(DataDescriptor(InMemoryData("data".toByteArray()), MediaTypeConstants.TEXT_PLAIN)),
                              MediaTypeConstants.TEXT_PLAIN,
                              MapParameters.empty()).let {
            assertThat(it).hasSize(1)
            assertThat(it.first()).isEqualTo(TransformedDataDescriptor(InMemoryData("data".toByteArray()),
                                                                       MapMetadata.empty()))
        }
    }

    @Test
    fun canTransform() {
        assertThat(transformer.canTransform(emptyList(), MediaTypeConstants.TEXT_PLAIN, MapParameters.empty()))
                .isEqualTo(true)

        assertThat(transformer.canTransform(emptyList(), MediaTypeConstants.APPLICATION_PDF, MapParameters.empty()))
                .isEqualTo(false)
    }
}