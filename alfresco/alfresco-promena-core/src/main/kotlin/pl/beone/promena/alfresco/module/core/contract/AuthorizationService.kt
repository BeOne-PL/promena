package pl.beone.promena.alfresco.module.core.contract

interface AuthorizationService {

    fun getCurrentUser(): String

    fun <T> runAs(userName: String, toRun: () -> T): T
}