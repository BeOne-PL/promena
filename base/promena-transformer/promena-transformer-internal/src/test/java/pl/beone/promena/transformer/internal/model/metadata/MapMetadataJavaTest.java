package pl.beone.promena.transformer.internal.model.metadata;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

// The same parent as MapParameters. It is covered by MapParametersJavaTest and MapParametersTest tests
public class MapMetadataJavaTest {

    @Test
    public void empty() {
        assertThat(MapMetadata.empty().getAll())
                .isEmpty();
    }

    @Test
    public void of() {
        assertThat(MapMetadata.of(Map.ofEntries(entry("key", "value"),
                                                entry("key2", "value2")))
                              .getAll())
                .containsOnly(entry("key", "value"),
                              entry("key2", "value2"));
    }

}
