package pl.beone.promena.transformer.contract.transformation;

import org.junit.Test;
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType;
import pl.beone.promena.transformer.contract.model.Parameters;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN;
import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_XML;

public class TransformationBuilderTest {

    @Test
    public void build_zeroTransformations_shouldThrowIllegalArgumentException() {
        assertThatThrownBy(() -> new TransformationBuilder().build())
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transformation must consist of at least one transformer");
    }

    @Test
    public void build_oneSingleTransformation() {
        String id = "test";
        MediaType mediaType = TEXT_PLAIN;
        Parameters parameters = mock(Parameters.class);

        Transformation.Single singleTransformation = Transformation.Single.of(id, mediaType, parameters);
        assertThat(
                new TransformationBuilder()
                        .next(singleTransformation)
                        .build()
        )
                .isEqualTo(Transformation.Single.of(id, mediaType, parameters));
    }

    @Test
    public void build_twoSingleTransformations() {
        Transformation.Single singleTransformation = Transformation.Single.of("test", TEXT_PLAIN, mock(Parameters.class));
        Transformation.Single singleTransformation2 = Transformation.Single.of("test2", TEXT_XML, mock(Parameters.class));
        assertThat(
                new TransformationBuilder()
                        .next(singleTransformation)
                        .next(singleTransformation2)
                        .build()
        )
                .isEqualTo(Transformation.Composite.of(Arrays.asList(singleTransformation, singleTransformation2)));
    }
}
