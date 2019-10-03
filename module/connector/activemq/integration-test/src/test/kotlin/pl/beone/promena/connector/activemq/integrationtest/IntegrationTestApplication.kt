package pl.beone.promena.connector.activemq.integrationtest

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(
    scanBasePackages = [
        "pl.beone.promena.connector.activemq.integrationtest",
        "pl.beone.promena.connector.activemq.configuration"
    ]
)
class IntegrationTestApplication