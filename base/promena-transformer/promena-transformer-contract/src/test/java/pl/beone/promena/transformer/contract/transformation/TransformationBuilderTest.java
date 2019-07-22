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
import static pl.beone.promena.transformer.contract.transformation.TransformationDsl.singleTransformation;

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

        Transformation.Single singleTransformation = singleTransformation(id, mediaType, parameters);
        assertThat(new TransformationBuilder()
                           .add(singleTransformation)
                           .build())
                .isEqualTo(new Transformation.Single(id, mediaType, parameters));
    }

    @Test
    public void build_twoSingleTransformations() {
        Transformation.Single singleTransformation = singleTransformation("test", TEXT_PLAIN, mock(Parameters.class));
        Transformation.Single singleTransformation2 = singleTransformation("test2", TEXT_XML, mock(Parameters.class));
        assertThat(new TransformationBuilder()
                           .add(singleTransformation)
                           .add(singleTransformation2)
                           .build())
                .isEqualTo(new Transformation.Composite(Arrays.asList(singleTransformation, singleTransformation2)));
    }
}
