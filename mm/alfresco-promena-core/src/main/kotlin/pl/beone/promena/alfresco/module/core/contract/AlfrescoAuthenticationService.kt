package pl.beone.promena.alfresco.module.core.contract

interface AlfrescoAuthenticationService {

    fun getCurrentUser(): String

    fun <T> runAs(userName: String, toRun: () -> T): T
}