package pl.beone.promena.transformer.contract.data;

import org.junit.Test;
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType;
import pl.beone.promena.transformer.contract.model.Data;
import pl.beone.promena.transformer.contract.model.Metadata;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML;
import static pl.beone.promena.transformer.contract.data.DataDescriptorDsl.singleDataDescriptor;

public class DataDescriptorBuilderTest {

    @Test
    public void build_zeroDataDescriptors() {
        assertThat(new DataDescriptorBuilder()
                           .build())
                .isEqualTo(DataDescriptor.Empty.INSTANCE);
    }

    @Test
    public void build_oneSingleDataDescriptor() {
        Data data = mock(Data.class);
        MediaType mediaType = TEXT_PLAIN;
        Metadata metadata = mock(Metadata.class);

        DataDescriptor.Single singleDataDescriptor = singleDataDescriptor(data, mediaType, metadata);
        assertThat(new DataDescriptorBuilder()
                           .add(singleDataDescriptor)
                           .build())
                .isEqualTo(new DataDescriptor.Single(data, mediaType, metadata));
    }

    @Test
    public void build_twoSingleDataDescriptors() {
        DataDescriptor.Single singleDataDescriptor = singleDataDescriptor(mock(Data.class), TEXT_PLAIN, mock(Metadata.class));
        DataDescriptor.Single singleDataDescriptor2 = singleDataDescriptor(mock(Data.class), TEXT_XML, mock(Metadata.class));
        assertThat(new DataDescriptorBuilder()
                           .add(singleDataDescriptor)
                           .add(singleDataDescriptor2)
                           .build())
                .isEqualTo(new DataDescriptor.Multi(Arrays.asList(singleDataDescriptor, singleDataDescriptor2)));
    }
}
