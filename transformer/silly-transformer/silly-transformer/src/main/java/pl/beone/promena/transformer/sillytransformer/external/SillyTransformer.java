package pl.beone.promena.transformer.sillytransformer.external;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerException;
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException;
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType;
import pl.beone.promena.transformer.contract.Transformer;
import pl.beone.promena.transformer.contract.descriptor.DataDescriptor;
import pl.beone.promena.transformer.contract.descriptor.TransformedDataDescriptor;
import pl.beone.promena.transformer.contract.model.Parameters;
import pl.beone.promena.transformer.internal.model.metadata.MapMetadata;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SillyTransformer implements Transformer, Serializable {

    private Logger logger = LoggerFactory.getLogger(SillyTransformer.class);

    @NotNull
    @Override
    public List<TransformedDataDescriptor> transform(@NotNull List<DataDescriptor> dataDescriptors,
                                                     @NotNull MediaType targetMediaType,
                                                     @NotNull Parameters parameters) throws TransformerException, TransformerTimeoutException {
        try {
            logger.info("Waiting <2> seconds. It's my only skill. I'm an idiot...");
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return dataDescriptors.stream()
                              .map(it -> new TransformedDataDescriptor(it.getData(), new MapMetadata(Collections.emptyMap())))
                              .collect(Collectors.toList());
    }

    @Override
    public boolean canTransform(@NotNull List<DataDescriptor> dataDescriptors, @NotNull MediaType targetMediaType, @NotNull Parameters parameters) {
        return true;
    }
}
