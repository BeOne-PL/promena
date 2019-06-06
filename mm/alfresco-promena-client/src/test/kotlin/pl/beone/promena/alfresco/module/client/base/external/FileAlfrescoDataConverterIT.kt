package pl.beone.promena.alfresco.module.client.base.external

import io.kotlintest.matchers.string.shouldContain
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import org.alfresco.rad.test.AlfrescoTestRunner
import org.junit.Test
import org.junit.runner.RunWith
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.internal.model.data.FileData
import pl.beone.promena.transformer.internal.model.data.InMemoryData

@RunWith(AlfrescoTestRunner::class)
class FileAlfrescoDataConverterIT : AbstractUtilsAlfrescoIT() {

    @Test
    fun createData_withoutLocationInMemoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode().apply {
            saveContent(MediaTypeConstants.TEXT_PLAIN, "test")
        }

        FileAlfrescoDataConverter(null).createData(node.getContentReader()).let {
            it.getBytes() shouldBe "test".toByteArray()
            shouldThrow<UnsupportedOperationException> { it.getLocation() }
        }
    }

    @Test
    fun createData_withLocationFileData() {
        val tmpDir = createTempDir()

        try {
            val node = createOrGetIntegrationTestsFolder().createNode().apply {
                saveContent(MediaTypeConstants.TEXT_PLAIN, "test")
            }

            FileAlfrescoDataConverter(tmpDir.toURI())
                    .createData(node.getContentReader()).let {
                        it.getBytes() shouldBe "test".toByteArray()
                        it.getLocation().toString() shouldContain  tmpDir.toString()
                    }
        } finally {
            tmpDir.delete()
        }
    }

    @Test
    fun saveDataInContentWriter_inMemoryData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val data = InMemoryData("test".toByteArray())

        FileAlfrescoDataConverter(null)
                .saveDataInContentWriter(data, node.getContentWriter())

        node.readContent() shouldBe "test".toByteArray()
    }

    @Test
    fun saveDataInContentWriter_fileData() {
        val node = createOrGetIntegrationTestsFolder().createNode()

        val file = createTempFile().apply {
            writeText("test")
        }

        val data = FileData(file.toURI())

        try {
            FileAlfrescoDataConverter(null)
                    .saveDataInContentWriter(data, node.getContentWriter())

            node.readContent() shouldBe "test".toByteArray()
        } finally {
            file.delete()
        }
    }
}