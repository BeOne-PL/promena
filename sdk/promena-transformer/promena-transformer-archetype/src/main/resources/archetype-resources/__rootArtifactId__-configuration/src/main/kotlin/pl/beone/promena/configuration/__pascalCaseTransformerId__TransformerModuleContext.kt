package pl.beone.promena.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan(basePackages = ["pl.beone.promena.transformer.textsurrounder.configuration"])
@PropertySource("classpath:transformer-${transformerId}.properties")
class ${pascalCaseTransformerId}TransformerModuleContext