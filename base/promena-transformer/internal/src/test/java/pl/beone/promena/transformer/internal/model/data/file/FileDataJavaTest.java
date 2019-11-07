package pl.beone.promena.transformer.internal.model.data.file;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class FileDataJavaTest {

    private static final String fileString = "bytes";
    private static final byte[] fileBytes = fileString.getBytes();

    @Test
    void of_file() {
        File file = FileUtils.createTempFile(fileString);

        assertThat(FileData.of(file).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    void of_inputStreamAndDirectory() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        File directoryFile = FileUtils.createTempDir();

        assertThat(FileData.of(inputStream, directoryFile).getBytes())
                .isEqualTo(fileBytes);
    }
}
