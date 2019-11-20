package ${package}.configuration

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import javax.annotation.PostConstruct
import ${package}.${pascalCaseTransformerId}Transformer
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

@Configuration
class ${pascalCaseTransformerId}TransformerLogger(
    private val settings: ${pascalCaseTransformerId}TransformerSettings,
    private val defaultParameters: ${pascalCaseTransformerId}TransformerDefaultParameters
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Run <${${pascalCaseTransformerId}Transformer::class.java.canonicalName}> with <$settings> and <$defaultParameters>"
        }
    }
}