package ${package}

import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.matchers.instanceOf
import io.kotlintest.should
import io.kotlintest.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.lib.dockertestrunner.external.DockerTestRunner
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.APPLICATION_PDF
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor
import pl.beone.promena.transformer.internal.communication.MapCommunicationParameters
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.MemoryData
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata
import pl.beone.promena.transformer.internal.model.parameters.MapParameters

@RunWith(DockerTestRunner::class)
class ${transformerClassName}Test {

    @Test
    fun transform_memoryInternalCommunication() {
        val transformer = ${transformerClassName}(MapCommunicationParameters.create("memory"))

        val metadata = MapMetadata.empty()

        transformer.transform(listOf(DataDescriptor("data".toMemoryData(), TEXT_PLAIN, metadata)), TEXT_PLAIN, MapParameters.empty())
                .let {
                    it shouldHaveSize 1

                    val (transformedData, transformedMetadata) = it.first()
                    transformedData should instanceOf(MemoryData::class)
                    transformedData.getBytes() shouldBe "data#".toByteArray()
                    transformedMetadata shouldBe metadata
                }
    }

    @Test
    fun transform_fileInternalCommunication() {
        val transformer = ${transformerClassName}(MapCommunicationParameters.create("file"))

        val metadata = MapMetadata.empty()

        transformer.transform(listOf(DataDescriptor("data".toFileData(), TEXT_PLAIN, metadata)), TEXT_PLAIN, MapParameters.empty())
                .let {
                    it shouldHaveSize 1

                    val (transformedData, transformedMetadata) = it.first()
                    transformedData should instanceOf(FileData::class)
                    transformedData.getBytes() shouldBe "data#".toByteArray()
                    transformedMetadata shouldBe metadata
                }
    }

    @Test
    fun canTransform() {
        val transformer = ${transformerClassName}(MapCommunicationParameters.empty())

        transformer.canTransform(emptyList(), TEXT_PLAIN, MapParameters.empty()) shouldBe true

        transformer.canTransform(emptyList(), APPLICATION_PDF, MapParameters.empty()) shouldBe false
    }

    private fun String.toMemoryData(): MemoryData =
            MemoryData(toByteArray())

    private fun String.toFileData(): FileData =
            FileData(createTempFile().apply {
                writeText(this@toFileData)
            }.toURI())

}