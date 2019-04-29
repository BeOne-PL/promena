package pl.beone.promena.transformer.sillytransformer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.beone.promena.transformer.sillytransformer.external.SillyTransformer;

@Configuration
public class SillyTransformerConfiguration {

    @Bean
    public SillyTransformer sillyTransformer() {
        return new SillyTransformer();
    }

}
