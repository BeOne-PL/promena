package pl.beone.promena.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@ComponentScan(basePackages = ["${package}.configuration"])
@PropertySource("classpath:transformer-${rootArtifactId}.properties")
class ${pascalCaseTransformerId}TransformerModuleContext