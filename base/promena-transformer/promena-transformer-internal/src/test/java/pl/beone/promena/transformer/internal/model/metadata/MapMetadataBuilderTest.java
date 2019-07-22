package pl.beone.promena.transformer.internal.model.metadata;

import org.junit.Test;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

public class MapMetadataBuilderTest {

    @Test
    public void build() {
        assertThat(new MapMetadataBuilder()
                           .add("key", "value")
                           .add("key2", "value2")
                           .build()
                           .getAll())
                .containsOnly(entry("key", "value"),
                              entry("key2", "value2"));
    }
}
