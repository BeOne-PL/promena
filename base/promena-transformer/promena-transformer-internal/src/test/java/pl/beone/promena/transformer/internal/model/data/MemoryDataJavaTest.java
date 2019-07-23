package pl.beone.promena.transformer.internal.model.data;

import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MemoryDataJavaTest {

    private static byte[] bytes = "bytes".getBytes();


    @Test
    public void of_bytes() {
        assertThat(MemoryData.of(bytes).getBytes())
                .isEqualTo(bytes);
    }

    @Test
    public void of_inputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        assertThat(MemoryData.of(inputStream).getBytes())
                .isEqualTo(bytes);
    }
}
