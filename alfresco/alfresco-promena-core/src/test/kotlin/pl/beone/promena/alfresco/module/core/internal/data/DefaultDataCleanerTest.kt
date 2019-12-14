package pl.beone.promena.alfresco.module.core.internal.data

import io.kotlintest.shouldBe
import io.kotlintest.shouldNotThrowAny
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import pl.beone.promena.transformer.applicationmodel.exception.data.DataDeleteException
import pl.beone.promena.transformer.contract.model.data.Data
import pl.beone.promena.transformer.internal.model.data.file.toFileData
import pl.beone.promena.transformer.internal.model.data.memory.toMemoryData

class DefaultDataCleanerTest {

    @Test
    fun clean() {
        val file = createTempFile()

        shouldNotThrowAny {
            DefaultDataCleaner().clean(
                listOf(
                    "test".toMemoryData(),
                    file.toFileData(),
                    mockk<Data> { every { delete() } throws DataDeleteException("no matter") },
                    mockk<Data> { every { delete() } throws UnsupportedOperationException() }
                )
            )
        }

        file.exists() shouldBe false
    }
}