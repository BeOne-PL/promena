package pl.beone.promena.transformer.internal.model.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class FileDataJavaTest {

    private static final String fileString = "bytes";
    private static final byte[] fileBytes = fileString.getBytes();
    private static final File file = FileUtils.createTempFile(fileString);

    @Test
    void of_uri() {
        assertThat(FileData.of(file.toURI()).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    void of_file() {
        assertThat(FileData.of(file).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    void of_inputStreamAndDirectoryUri() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        URI directoryUri = FileUtils.createTempDir().toURI();

        assertThat(FileData.of(inputStream, directoryUri).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    void of_inputStreamAndDirectoryFile() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        File directoryFile = FileUtils.createTempDir();

        assertThat(FileData.of(inputStream, directoryFile).getBytes())
                .isEqualTo(fileBytes);
    }
}
