package pl.beone.promena.transformer.internal.model.parameters;

import org.junit.Test;
import pl.beone.promena.transformer.contract.model.Parameters;

import java.time.Duration;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

public class MapParametersBuilderTest {

    @Test
    public void build() {
        assertThat(new MapParametersBuilder()
                           .add("key", "value")
                           .add("key2", "value2")
                           .addTimeout(Duration.ofMillis(100))
                           .build()
                           .getAll())
                .containsOnly(entry("key", "value"),
                              entry("key2", "value2"),
                              entry(Parameters.TIMEOUT, Duration.ofMillis(100)));
    }
}
