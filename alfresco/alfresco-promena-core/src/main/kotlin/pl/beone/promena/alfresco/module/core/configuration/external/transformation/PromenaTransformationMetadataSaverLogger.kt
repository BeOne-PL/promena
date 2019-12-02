package pl.beone.promena.alfresco.module.core.configuration.external.transformation

import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.contract.transformation.PromenaTransformationMetadataSaver
import javax.annotation.PostConstruct

@Configuration
class PromenaTransformationMetadataSaverLogger(
    private val promenaTransformationMetadataSavers: List<PromenaTransformationMetadataSaver>
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @PostConstruct
    private fun log() {
        logger.info {
            "Found <${promenaTransformationMetadataSavers.size}> metadata saver(s): ${promenaTransformationMetadataSavers.map { it::class.simpleName }}"
        }
    }
}