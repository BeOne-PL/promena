package pl.beone.promena.alfresco.module.core.external.node

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.rad.test.AlfrescoTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.alfresco.module.core.external.AbstractUtilsAlfrescoIT
import pl.beone.promena.communication.file.model.contract.FileCommunicationParametersConstants
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParametersConstants
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.internal.model.data.file.FileData
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData

@RunWith(AlfrescoTestRunner::class)
class MemoryOrFileDataConverterTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun createData_withoutLocationMemoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode().apply {
            saveContent(TEXT_PLAIN, "test")
        }

        with(
            MemoryOrFileDataConverter(MemoryCommunicationParametersConstants.ID)
                .createData(node.getContentReader())
        ) {
            getBytes() shouldBe "test".toByteArray()
            shouldThrow<UnsupportedOperationException> { getLocation() }
        }
    }

    @Test
    fun createData_withLocationFileData() {
        val tmpDir = createTempDir()

        try {
            val node = createOrGetIntegrationTestsFolder().createNode().apply {
                saveContent(TEXT_PLAIN, "test")
            }

            with(
                MemoryOrFileDataConverter(FileCommunicationParametersConstants.ID, tmpDir)
                    .createData(node.getContentReader())
            ) {
                getBytes() shouldBe "test".toByteArray()
                getLocation().toString() shouldContain tmpDir.toString()
            }
        } finally {
            tmpDir.delete()
        }
    }

    @Test
    fun saveDataInContentWriter_memoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val data = "test".toMemoryData()

        MemoryOrFileDataConverter(MemoryCommunicationParametersConstants.ID, null)
            .saveDataInContentWriter(data, node.getContentWriter())

        node.readContent() shouldBe "test".toByteArray()
    }

    @Test
    fun saveDataInContentWriter_fileData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val data = FileData.of("test".byteInputStream(), createTempDir())

        MemoryOrFileDataConverter(MemoryCommunicationParametersConstants.ID, null)
            .saveDataInContentWriter(data, node.getContentWriter())

        java.io.File(data.getLocation()).exists() shouldBe true
        node.readContent() shouldBe "test".toByteArray()
    }
}