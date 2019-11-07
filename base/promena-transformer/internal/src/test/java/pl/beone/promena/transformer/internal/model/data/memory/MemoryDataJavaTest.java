package pl.beone.promena.transformer.internal.model.data.memory;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.assertj.core.api.Assertions.assertThat;

class MemoryDataJavaTest {

    private static final byte[] bytes = "bytes".getBytes();


    @Test
    void of_bytes() {
        assertThat(MemoryData.of(bytes).getBytes())
                .isEqualTo(bytes);
    }

    @Test
    void of_inputStream() {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

        assertThat(MemoryData.of(inputStream).getBytes())
                .isEqualTo(bytes);
    }
}
