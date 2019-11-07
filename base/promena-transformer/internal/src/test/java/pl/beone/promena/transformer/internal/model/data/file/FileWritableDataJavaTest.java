package pl.beone.promena.transformer.internal.model.data.file;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class FileWritableDataJavaTest {

    @Test
    void ofEmptyFile() {
        File file = FileUtils.createTempFile("");

        assertThat(FileWritableData.ofEmptyFile(file).getBytes())
                .isEqualTo("".getBytes());
    }

    @Test
    void ofDirectory() {
        assertThat(FileWritableData.ofDirectory(FileUtils.createTempDir()).getBytes())
                .isEqualTo("".getBytes());
    }
}
