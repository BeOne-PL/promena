package pl.beone.promena.alfresco.module.core.external

import org.alfresco.repo.security.authentication.AuthenticationUtil
import org.alfresco.service.cmr.security.AuthenticationService
import pl.beone.promena.alfresco.module.core.contract.AuthorizationService

class SecurityAuthorizationService(
    private val authenticationService: AuthenticationService
) : AuthorizationService {

    override fun getCurrentUser(): String =
        authenticationService.currentUserName

    override fun <T> runAs(userName: String, toRun: () -> T): T =
        AuthenticationUtil.runAs({ toRun() }, userName)
}