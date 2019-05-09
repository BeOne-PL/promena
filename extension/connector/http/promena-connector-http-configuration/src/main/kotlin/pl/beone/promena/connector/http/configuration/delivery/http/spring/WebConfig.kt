package pl.beone.promena.connector.http.configuration.delivery.http.spring

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    // TODO verify
    override fun configureContentNegotiation(configurer: ContentNegotiationConfigurer) {
        configurer.ignoreAcceptHeader(true)
    }
}