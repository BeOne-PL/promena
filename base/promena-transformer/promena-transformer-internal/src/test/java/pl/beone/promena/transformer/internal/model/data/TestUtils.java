package pl.beone.promena.transformer.internal.model.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

class TestUtils {

    static File createTempFile(String string) {
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
    static File createTempDir() {
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
