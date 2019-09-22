package pl.beone.promena.alfresco.module.rendition.external.thumbnail

import mu.KotlinLogging
import org.alfresco.repo.lock.JobLockService
import org.alfresco.repo.rendition2.RenditionDefinitionRegistry2
import org.alfresco.repo.tenant.TenantAdminService
import org.alfresco.repo.thumbnail.ThumbnailDefinition
import org.alfresco.repo.thumbnail.ThumbnailRegistry
import org.alfresco.repo.thumbnail.ThumbnailRenditionConvertor
import org.alfresco.service.cmr.rendition.RenditionService
import org.alfresco.service.cmr.repository.ContentService
import org.alfresco.service.cmr.repository.NodeRef
import org.alfresco.service.transaction.TransactionService
import org.alfresco.transform.client.model.config.TransformServiceRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ApplicationContextEvent
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.AlfrescoPromenaRenditionTransformationNotSupportedException
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.NoSuchAlfrescoPromenaRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.AlfrescoPromenaRenditionDefinitionGetter
import pl.beone.promena.alfresco.module.rendition.extension.getMediaType

internal class PromenaThumbnailRegistry(
    private val contentService: ContentService,
    private val alfrescoPromenaRenditionDefinitionGetter: AlfrescoPromenaRenditionDefinitionGetter
) : ThumbnailRegistry() {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val thumbnailDefinitions =
        alfrescoPromenaRenditionDefinitionGetter.getAll()
            .map(::createThumbnailDefinition)

    private val renditionNameToThumbnailDefinitionMap =
        thumbnailDefinitions.map { it.name to it }
            .toMap()

    private fun createThumbnailDefinition(renditionDefinition: AlfrescoPromenaRenditionDefinition) =
        PromenaThumbnailDefinition(renditionDefinition)

    override fun getThumbnailDefinition(thumbnailName: String): ThumbnailDefinition? =
        renditionNameToThumbnailDefinitionMap[thumbnailName]

    override fun setRenditionService(renditionService: RenditionService?) {
        // deliberately omitted
    }

    override fun isThumbnailDefinitionAvailable(
        sourceUrl: String?,
        sourceMimetype: String?,
        sourceSize: Long,
        sourceNodeRef: NodeRef,
        thumbnailDefinition: ThumbnailDefinition
    ): Boolean =
        try {
            alfrescoPromenaRenditionDefinitionGetter
                .getByRenditionName(thumbnailDefinition.name)
                .getTransformation(sourceNodeRef, contentService.getMediaType(sourceNodeRef))
            true
        } catch (e: NoSuchAlfrescoPromenaRenditionDefinitionException) {
            logger.debug { "Couldn't get rendition for <$sourceNodeRef> | ${e.message}" }
            false
        } catch (e: AlfrescoPromenaRenditionTransformationNotSupportedException) {
            logger.debug { e.message }
            false
        }

    override fun isThumbnailDefinitionAvailable(
        sourceUrl: String?,
        sourceMimeType: String?,
        sourceSize: Long,
        thumbnailDefinition: ThumbnailDefinition
    ): Boolean =
        try {
            alfrescoPromenaRenditionDefinitionGetter
                .getByRenditionName(thumbnailDefinition.name)
            true
        } catch (e: NoSuchAlfrescoPromenaRenditionDefinitionException) {
            logger.debug { e.message }
            false
        }

    override fun onApplicationEvent(event: ApplicationContextEvent) {
        // deliberately omitted
    }

    override fun setJobLockService(jobLockService: JobLockService?) {
        // deliberately omitted
    }

    override fun getThumbnailRenditionConvertor(): ThumbnailRenditionConvertor {
        TODO("implement")
    }

    override fun redeploy(): Boolean =
        false

    override fun setTransformServiceRegistry(transformServiceRegistry: TransformServiceRegistry?) {
        // deliberately omitted
    }

    override fun setThumbnailRenditionConvertor(thumbnailRenditionConvertor: ThumbnailRenditionConvertor?) {
        // deliberately omitted
    }

    override fun initThumbnailDefinitions() {
        // deliberately omitted
    }

    override fun setRedeployStaticDefsOnStartup(redeployStaticDefsOnStartup: Boolean) {
        // deliberately omitted
    }

    override fun setContentService(contentService: ContentService?) {
        // deliberately omitted
    }

    override fun setRenditionDefinitionRegistry2(renditionDefinitionRegistry2: RenditionDefinitionRegistry2?) {
        // deliberately omitted
    }

    override fun getMaxSourceSizeBytes(sourceMimetype: String?, thumbnailDefinition: ThumbnailDefinition?): Long =
        Long.MAX_VALUE

    override fun getThumnailDefintions(mimetype: String?): List<ThumbnailDefinition> =
        thumbnailDefinitions

    override fun setTransactionService(transactionService: TransactionService?) {
        // deliberately omitted
    }

    override fun setTenantAdminService(tenantAdminService: TenantAdminService?) {
        // deliberately omitted
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        // deliberately omitted
    }

    override fun setThumbnailDefinitions(thumbnailDefinitions: MutableList<ThumbnailDefinition>?) {
        // deliberately omitted
    }

    override fun addThumbnailDefinition(thumbnailDetails: ThumbnailDefinition?) {
        // deliberately omitted
    }

    override fun getThumbnailDefinitions(): List<ThumbnailDefinition> =
        thumbnailDefinitions

    override fun getThumbnailDefinitions(mimetype: String?): List<ThumbnailDefinition> =
        thumbnailDefinitions

    override fun getThumbnailDefinitions(mimetype: String?, sourceSize: Long): List<ThumbnailDefinition> =
        thumbnailDefinitions

    override fun getThumbnailDefinitions(sourceUrl: String?, mimetype: String?, sourceSize: Long): List<ThumbnailDefinition> =
        thumbnailDefinitions
}