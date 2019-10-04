package pl.beone.promena.core.external.akka.transformation;

import org.jetbrains.annotations.NotNull;
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformationNotSupportedException;
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType;
import pl.beone.promena.transformer.contract.Transformer;
import pl.beone.promena.transformer.contract.data.DataDescriptor;
import pl.beone.promena.transformer.contract.data.TransformedDataDescriptor;
import pl.beone.promena.transformer.contract.model.Data;
import pl.beone.promena.transformer.contract.model.Metadata;
import pl.beone.promena.transformer.contract.model.Parameters;
import pl.beone.promena.transformer.internal.model.data.MemoryData;
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN;
import static pl.beone.promena.transformer.contract.data.TransformedDataDescriptorDsl.singleTransformedDataDescriptor;
import static pl.beone.promena.transformer.contract.data.TransformedDataDescriptorDsl.transformedDataDescriptor;

public class JavaTextAppenderTransformer implements Transformer {

    @NotNull
    @Override
    public TransformedDataDescriptor transform(@NotNull DataDescriptor dataDescriptor,
                                               @NotNull MediaType targetMediaType,
                                               @NotNull Parameters parameters) {
        return transformedDataDescriptor(
                dataDescriptor.getDescriptors().stream()
                              .map(it -> singleTransformedDataDescriptor(
                                      MemoryData.of(addHashAtTheEnd(it.getData(), getAppend(parameters)).getBytes()),
                                      addTransformerId(it.getMetadata())
                              ))
                              .collect(Collectors.toList())
        );
    }

    @NotNull
    private String getAppend(Parameters parameters) {
        return parameters.get("append", String.class);
    }

    @NotNull
    private String addHashAtTheEnd(Data data, String sign) {
        return new String(data.getBytes()) + sign;
    }

    @NotNull
    private Metadata addTransformerId(Metadata metadata) {
        Map<String, Object> elements = new HashMap<>(metadata.getAll());
        elements.put("java-text appender-transformer", true);
        return MapMetadata.of(elements);
    }

    @Override
    public void isSupported(@NotNull DataDescriptor dataDescriptor,
                            @NotNull MediaType targetMediaType,
                            @NotNull Parameters parameters) throws TransformationNotSupportedException {
        dataDescriptor.getDescriptors().forEach(it -> {
            if (!it.getMediaType().equals(TEXT_PLAIN)) {
                throwException();
            }
        });

        if (!targetMediaType.equals(TEXT_PLAIN)) {
            throwException();
        }
    }

    private void throwException() {
        throw new TransformationNotSupportedException("Only the transformation from text/plain to text/plain is supported");
    }
}
