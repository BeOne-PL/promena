package pl.beone.promena.alfresco.module.rendition.external.thumbnail

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
import pl.beone.promena.alfresco.module.rendition.applicationmodel.exception.PromenaNoSuchRenditionDefinitionException
import pl.beone.promena.alfresco.module.rendition.contract.PromenaAlfrescoRenditionDefinition
import pl.beone.promena.alfresco.module.rendition.contract.PromenaRenditionDefinitionManager

internal class PromenaThumbnailRegistry(
    private val promenaRenditionDefinitionManager: PromenaRenditionDefinitionManager
) : ThumbnailRegistry() {

    private val thumbnailDefinitions =
        promenaRenditionDefinitionManager.getAll()
            .map(::createThumbnailDefinition)

    private val renditionNameToThumbnailDefinitionMap =
        thumbnailDefinitions.map { it.name to it }
            .toMap()

    private fun createThumbnailDefinition(renditionName: PromenaAlfrescoRenditionDefinition) =
        PromenaThumbnailDefinition(renditionName.getRenditionName())

    override fun getThumbnailDefinition(thumbnailName: String): ThumbnailDefinition? =
        renditionNameToThumbnailDefinitionMap[thumbnailName]

    override fun setRenditionService(renditionService: RenditionService?) {
    }

    override fun isThumbnailDefinitionAvailable(
        sourceUrl: String?,
        sourceMimetype: String?,
        sourceSize: Long,
        sourceNodeRef: NodeRef?,
        thumbnailDefinition: ThumbnailDefinition
    ): Boolean =
        try {
            promenaRenditionDefinitionManager.getByRenditionName(thumbnailDefinition.name)
            true
        } catch (e: PromenaNoSuchRenditionDefinitionException) {
            false
        }

    override fun isThumbnailDefinitionAvailable(
        sourceUrl: String?,
        sourceMimeType: String?,
        sourceSize: Long,
        thumbnailDefinition: ThumbnailDefinition
    ): Boolean =
        isThumbnailDefinitionAvailable(sourceUrl, sourceMimeType, sourceSize, null, thumbnailDefinition)

    override fun onApplicationEvent(event: ApplicationContextEvent) {
    }

    override fun setJobLockService(jobLockService: JobLockService?) {
    }

    override fun getThumbnailRenditionConvertor(): ThumbnailRenditionConvertor {
        TODO("implement")
    }

    override fun redeploy(): Boolean =
        false

    override fun setTransformServiceRegistry(transformServiceRegistry: TransformServiceRegistry?) {
    }

    override fun setThumbnailRenditionConvertor(thumbnailRenditionConvertor: ThumbnailRenditionConvertor?) {
    }

    override fun initThumbnailDefinitions() {
    }

    override fun setRedeployStaticDefsOnStartup(redeployStaticDefsOnStartup: Boolean) {
    }

    override fun setContentService(contentService: ContentService?) {
    }

    override fun setRenditionDefinitionRegistry2(renditionDefinitionRegistry2: RenditionDefinitionRegistry2?) {
    }

    override fun getMaxSourceSizeBytes(sourceMimetype: String?, thumbnailDefinition: ThumbnailDefinition?): Long =
        Long.MAX_VALUE

    override fun getThumnailDefintions(mimetype: String?): List<ThumbnailDefinition> =
        thumbnailDefinitions

    override fun setTransactionService(transactionService: TransactionService?) {
    }

    override fun setTenantAdminService(tenantAdminService: TenantAdminService?) {
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
    }

    override fun setThumbnailDefinitions(thumbnailDefinitions: MutableList<ThumbnailDefinition>?) {
    }

    override fun addThumbnailDefinition(thumbnailDetails: ThumbnailDefinition?) {
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