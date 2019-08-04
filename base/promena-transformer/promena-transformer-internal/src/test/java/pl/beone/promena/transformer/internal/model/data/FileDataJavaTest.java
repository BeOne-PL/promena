package pl.beone.promena.transformer.internal.model.data;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class FileDataJavaTest {

    private static String fileString = "bytes";
    private static byte[] fileBytes = fileString.getBytes();
    private static File file = TestUtils.createTempFile(fileString);

    @Test
    public void of_uri() {
        assertThat(FileData.of(file.toURI()).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    public void of_file() {
        assertThat(FileData.of(file).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    public void of_inputStreamAndDirectoryUri() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        URI directoryUri = TestUtils.createTempDir().toURI();

        assertThat(FileData.of(inputStream, directoryUri).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    public void of_inputStreamAndDirectoryFile() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        File directoryFile = TestUtils.createTempDir();

        assertThat(FileData.of(inputStream, directoryFile).getBytes())
                .isEqualTo(fileBytes);
    }
}
