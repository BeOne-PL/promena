package pl.beone.promena.alfresco.module.core.configuration.external

import org.alfresco.service.cmr.security.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.core.external.SecurityAuthorizationService

@Configuration
class SecurityAuthorizationServiceContext {

    @Bean
    fun securityAuthorizationService(
        authenticationService: AuthenticationService
    ) =
        SecurityAuthorizationService(
            authenticationService
        )
}