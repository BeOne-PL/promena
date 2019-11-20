package ${package}.configuration

import io.kotlintest.shouldBe
import org.junit.Test
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.mock.env.MockEnvironment
import java.time.Duration
import ${package}.${pascalCaseTransformerId}TransformerDefaultParameters
import ${package}.${pascalCaseTransformerId}TransformerSettings

class ${pascalCaseTransformerId}TransformerConfigurationContextTest {

    @Test
    fun `setting context _ default parameters`() {
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
        applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java).let {
            it.hostname shouldBe "localhost"
            it.port shouldBe 8080
        }
        applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java).let {
            it.optional shouldBe null
            it.optionalLimitedValue shouldBe null
            it.timeout shouldBe null
        }
    }

    @Test
    fun `setting context _ all values`() {
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
        applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java).let {
            it.hostname shouldBe "localhost"
            it.port shouldBe 8080
        }
        applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java).let {
            it.optional shouldBe "value"
            it.optionalLimitedValue shouldBe 1
            it.timeout shouldBe Duration.ofMinutes(5)
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