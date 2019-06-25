package pl.beone.promena.connector.messagebroker.integrationtest

import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication(scanBasePackages = [
    "pl.beone.promena.connector.messagebroker.integrationtest",
    "pl.beone.promena.connector.messagebroker.configuration"
])
class IntegrationTestApplication