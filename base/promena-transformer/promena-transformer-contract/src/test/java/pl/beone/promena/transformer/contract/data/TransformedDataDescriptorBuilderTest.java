package pl.beone.promena.transformer.contract.data;

import org.junit.jupiter.api.Test;
import pl.beone.promena.transformer.contract.model.Data;
import pl.beone.promena.transformer.contract.model.Metadata;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class TransformedDataDescriptorBuilderTest {

    @Test
    public void build_zeroTransformedDataDescriptors() {
        assertThat(new TransformedDataDescriptorBuilder()
                .build())
                .isEqualTo(TransformedDataDescriptor.Empty.INSTANCE);
    }

    @Test
    public void build_oneSingleTransformedDataDescriptor() {
        Data data = mock(Data.class);
        Metadata metadata = mock(Metadata.class);

        TransformedDataDescriptor.Single singleTransformedDataDescriptor = TransformedDataDescriptor.Single.of(data, metadata);
        assertThat(
                new TransformedDataDescriptorBuilder()
                        .add(singleTransformedDataDescriptor)
                        .build()
        )
                .isEqualTo(TransformedDataDescriptor.Single.of(data, metadata));
    }

    @Test
    public void build_twoSingleTransformedDataDescriptors() {
        TransformedDataDescriptor.Single singleTransformedDataDescriptor = TransformedDataDescriptor.Single.of(mock(Data.class), mock(Metadata.class));
        TransformedDataDescriptor.Single singleTransformedDataDescriptor2 = TransformedDataDescriptor.Single.of(mock(Data.class), mock(Metadata.class));
        assertThat(
                new TransformedDataDescriptorBuilder()
                        .add(singleTransformedDataDescriptor)
                        .add(singleTransformedDataDescriptor2)
                        .build()
        )
                .isEqualTo(TransformedDataDescriptor.Multi.of(Arrays.asList(singleTransformedDataDescriptor, singleTransformedDataDescriptor2)));
    }
}
