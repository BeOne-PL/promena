package pl.beone.promena.alfresco.module.client.messagebroker.external

import org.alfresco.rad.test.AlfrescoTestRunner
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
            assertThat(it.getBytes()).isEqualTo("test".toByteArray())
            assertThatThrownBy { it.getLocation() }.isExactlyInstanceOf(UnsupportedOperationException::class.java)
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
                        assertThat(it.getBytes()).isEqualTo("test".toByteArray())
                        assertThat(it.getLocation().toString()).contains(tmpDir.toString())
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

        assertThat(node.readContent()).isEqualTo("test".toByteArray())
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

            assertThat(node.readContent()).isEqualTo("test".toByteArray())
        } finally {
            file.delete()
        }
    }
}