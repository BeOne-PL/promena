package pl.beone.promena.alfresco.module.client.base.contract

interface AlfrescoAuthenticationService {

    fun getCurrentUser(): String

    fun <T> runAs(userName: String, toRun: () -> T): T
}