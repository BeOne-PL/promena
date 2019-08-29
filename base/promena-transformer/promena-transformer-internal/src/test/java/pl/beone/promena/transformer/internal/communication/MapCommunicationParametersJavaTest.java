package pl.beone.promena.transformer.internal.communication;

import org.junit.jupiter.api.Test;
import pl.beone.promena.transformer.contract.communication.CommunicationParameters;

import java.util.Map;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

// The same parent as MapParameters. It is covered by MapParametersJavaTest and MapParametersTest tests
public class MapCommunicationParametersJavaTest {

    @Test
    public void of_withoutParameters() {
        assertThat(MapCommunicationParameters.of("memory")
                                             .getAll())
                .containsOnly(entry(CommunicationParameters.ID, "memory"));
    }

    @Test
    public void of_withParameters() {
        assertThat(MapCommunicationParameters.of("memory",
                                                 Map.ofEntries(entry("key", "value"),
                                                               entry("key2", "value2")))
                                             .getAll())
                .containsOnly(entry(CommunicationParameters.ID, "memory"),
                              entry("key", "value"),
                              entry("key2", "value2"));
    }
}
