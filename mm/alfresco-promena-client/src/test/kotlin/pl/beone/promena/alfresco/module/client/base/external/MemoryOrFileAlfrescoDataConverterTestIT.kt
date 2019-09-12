package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.rad.test.AlfrescoTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.communication.file.model.contract.FileCommunicationParameters
import pl.beone.promena.communication.memory.model.contract.MemoryCommunicationParameters
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.toMemoryData

@RunWith(AlfrescoTestRunner::class)
class MemoryOrFileAlfrescoDataConverterTestIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun createData_withoutLocationMemoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode().apply {
            saveContent(MediaTypeConstants.TEXT_PLAIN, "test")
        }

        MemoryOrFileAlfrescoDataConverter(MemoryCommunicationParameters.ID).createData(node.getContentReader()).let {
            it.getBytes() shouldBe "test".toByteArray()
            shouldThrow<UnsupportedOperationException> {
                it.getLocation()
            }
        }
    }

    @Test
    fun createData_withLocationFileData() {
        val tmpDir = createTempDir()

        try {
            val node = createOrGetIntegrationTestsFolder().createNode().apply {
                saveContent(MediaTypeConstants.TEXT_PLAIN, "test")
            }

            MemoryOrFileAlfrescoDataConverter(FileCommunicationParameters.ID, tmpDir)
                .createData(node.getContentReader()).let {
                    it.getBytes() shouldBe "test".toByteArray()
                    it.getLocation().toString() shouldContain tmpDir.toString()
                }
        } finally {
            tmpDir.delete()
        }
    }

    @Test
    fun saveDataInContentWriter_memoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val data = "test".toMemoryData()

        MemoryOrFileAlfrescoDataConverter(MemoryCommunicationParameters.ID, null)
            .saveDataInContentWriter(data, node.getContentWriter())

        node.readContent() shouldBe
                "test".toByteArray()
    }

    @Test
    fun saveDataInContentWriter_fileData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val data = FileData.of("test".byteInputStream(), createTempDir())

        MemoryOrFileAlfrescoDataConverter(MemoryCommunicationParameters.ID, null)
            .saveDataInContentWriter(data, node.getContentWriter())

        java.io.File(data.getLocation()).exists() shouldBe false
        node.readContent() shouldBe
                "test".toByteArray()
    }
}