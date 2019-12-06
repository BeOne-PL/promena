package ${package}.configuration

import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.mock.env.MockEnvironment
import java.time.Duration
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

class ${pascalCaseTransformerId}TransformerConfigurationContextTest {

    @Test
    fun `setting context _ default`() {
        val environment = createEnvironment(
            mapOf(
                "transformer.${package}.settings.hostname" to "localhost",
                "transformer.${package}.settings.port" to "8080",

                "transformer.${package}.default.parameters.optional" to "",
                "transformer.${package}.default.parameters.optional-limited-value" to "",
                "transformer.${package}.default.parameters.timeout" to ""
            )
        )

        val applicationContext = createConfigApplicationContext(environment, ${pascalCaseTransformerId}TransformerConfigurationContext::class.java)
        with(applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java)) {
            hostname shouldBe "localhost"
            port shouldBe 8080
        }
        with(applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java)) {
            optional shouldBe null
            optionalLimitedValue shouldBe null
            timeout shouldBe null
        }
    }

    @Test
    fun `setting context _ all`() {
        val environment = createEnvironment(
            mapOf(
                "transformer.${package}.settings.hostname" to "localhost",
                "transformer.${package}.settings.port" to "8080",

                "transformer.${package}.default.parameters.optional" to "value",
                "transformer.${package}.default.parameters.optional-limited-value" to "1",
                "transformer.${package}.default.parameters.timeout" to "5m"
            )
        )

        val applicationContext = createConfigApplicationContext(environment, ${pascalCaseTransformerId}TransformerConfigurationContext::class.java)
        with(applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java)) {
            hostname shouldBe "localhost"
            port shouldBe 8080
        }
        with(applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java)) {
            optional shouldBe "value"
            optionalLimitedValue shouldBe 1
            timeout shouldBe Duration.ofMinutes(5)
        }
    }

    private fun createEnvironment(properties: Map<String, String>): MockEnvironment =
        MockEnvironment()
            .apply { properties.forEach { (key, value) -> setProperty(key, value) } }

    private fun createConfigApplicationContext(environment: ConfigurableEnvironment, clazz: Class<*>): AnnotationConfigApplicationContext =
        AnnotationConfigApplicationContext().apply {
            this.environment = environment
            register(clazz)
            refresh()
        }
}