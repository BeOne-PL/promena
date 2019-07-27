package pl.beone.promena.transformer.internal.model.data;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class FileDataJavaTest {

    private static String fileString = "bytes";
    private static byte[] fileBytes = fileString.getBytes();
    private static File file = createTempFile(fileString);

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
        URI directoryUri = createTempDir().toURI();

        assertThat(FileData.of(inputStream, directoryUri).getBytes())
                .isEqualTo(fileBytes);
    }

    @Test
    public void of_inputStreamAndDirectoryFile() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(fileBytes);
        File directoryFile = createTempDir();

        assertThat(FileData.of(inputStream, directoryFile).getBytes())
                .isEqualTo(fileBytes);
    }

    private static File createTempFile(String string) {
        try {
            File file = File.createTempFile("tmp", null, null);
            try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
                fileOutputStream.write(string.getBytes());
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static File createTempDir() {
        try {
            File file = File.createTempFile("tmp", null, null);
            file.delete();
            file.mkdir();
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
