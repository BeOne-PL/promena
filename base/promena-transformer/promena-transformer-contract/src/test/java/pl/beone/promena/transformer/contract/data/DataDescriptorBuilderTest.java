package pl.beone.promena.transformer.contract.data;

import org.junit.jupiter.api.Test;
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType;
import pl.beone.promena.transformer.contract.model.Data;
import pl.beone.promena.transformer.contract.model.Metadata;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML;

class DataDescriptorBuilderTest {

    @Test
    void build_zeroDataDescriptors() {
        assertThat(new DataDescriptorBuilder().build())
                .isEqualTo(DataDescriptor.Empty.INSTANCE);
    }

    @Test
    void build_oneSingleDataDescriptor() {
        Data data = mock(Data.class);
        MediaType mediaType = TEXT_PLAIN;
        Metadata metadata = mock(Metadata.class);

        DataDescriptor.Single singleDataDescriptor = DataDescriptor.Single.of(data, mediaType, metadata);
        assertThat(
                new DataDescriptorBuilder()
                        .add(singleDataDescriptor)
                        .build()
        )
                .isEqualTo(DataDescriptor.Single.of(data, mediaType, metadata));
    }

    @Test
    void build_twoSingleDataDescriptors() {
        DataDescriptor.Single singleDataDescriptor = DataDescriptor.Single.of(mock(Data.class), TEXT_PLAIN, mock(Metadata.class));
        DataDescriptor.Single singleDataDescriptor2 = DataDescriptor.Single.of(mock(Data.class), TEXT_XML, mock(Metadata.class));
        assertThat(
                new DataDescriptorBuilder()
                        .add(singleDataDescriptor)
                        .add(singleDataDescriptor2)
                        .build()
        )
                .isEqualTo(DataDescriptor.Multi.of(Arrays.asList(singleDataDescriptor, singleDataDescriptor2)));
    }
}
