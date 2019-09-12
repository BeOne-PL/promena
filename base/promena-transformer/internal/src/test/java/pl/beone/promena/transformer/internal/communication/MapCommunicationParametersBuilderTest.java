package pl.beone.promena.transformer.internal.communication;

import org.junit.jupiter.api.Test;
import pl.beone.promena.transformer.contract.communication.CommunicationParameters;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MapCommunicationParametersBuilderTest {

    @Test
    void build() {
        assertThat(new MapCommunicationParametersBuilder()
                           .id("memory")
                           .build()
                           .getAll())
                .containsOnly(entry(CommunicationParameters.ID, "memory"));

        assertThat(new MapCommunicationParametersBuilder()
                           .id("memory")
                           .add("key", "value")
                           .build()
                           .getAll())
                .containsOnly(entry(CommunicationParameters.ID, "memory"),
                              entry("key", "value"));
    }

    @Test
    void build_noId_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> new MapCommunicationParametersBuilder().build())
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Communication parameters must contain <id>");
    }
}
