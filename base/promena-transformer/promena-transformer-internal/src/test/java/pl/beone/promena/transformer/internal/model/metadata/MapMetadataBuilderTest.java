package pl.beone.promena.transformer.internal.model.metadata;

import org.junit.jupiter.api.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class MapMetadataBuilderTest {

    @Test
    void build() {
        assertThat(new MapMetadataBuilder()
                           .add("key", "value")
                           .add("key2", "value2")
                           .build()
                           .getAll())
                .containsOnly(entry("key", "value"),
                              entry("key2", "value2"));
    }
}
