package pl.beone.promena.alfresco.module.client.base.configuration.external

import org.alfresco.service.cmr.security.AuthenticationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import pl.beone.promena.alfresco.module.client.base.external.SecurityAlfrescoAuthenticationService

@Configuration
class SecurityAlfrescoAuthenticationServiceContext {

    @Bean
    fun securityAlfrescoAuthenticationService(
        authenticationService: AuthenticationService
    ) =
        SecurityAlfrescoAuthenticationService(
            authenticationService
        )
}