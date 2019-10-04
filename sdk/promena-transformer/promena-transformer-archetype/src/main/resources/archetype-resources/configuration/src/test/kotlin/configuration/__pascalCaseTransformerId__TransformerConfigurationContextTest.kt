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
    fun `setting context`() {
        val environment = createEnvironment(
            mapOf(
                "transformer.${package}.settings.example" to "value",

                "transformer.${package}.default.parameters.example2" to "value2",
                "transformer.${package}.default.parameters.timeout" to "5m"
            )
        )

        val applicationContext = createConfigApplicationContext(environment, ${pascalCaseTransformerId}TransformerConfigurationContext::class.java)
        applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java).let {
            it.example shouldBe "value"
        }
        applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java).let {
            it.example2 shouldBe "value2"
            it.timeout shouldBe Duration.ofMinutes(5)
        }
    }

    @Test
    fun `setting context _ empty timeout`() {
        val environment = createEnvironment(
            mapOf(
                "transformer.${package}.settings.example" to "value",

                "transformer.${package}.default.parameters.example2" to "value2",
                "transformer.${package}.default.parameters.timeout" to ""
            )
        )

        val applicationContext = createConfigApplicationContext(environment, ${pascalCaseTransformerId}TransformerConfigurationContext::class.java)
        applicationContext.getBean(${pascalCaseTransformerId}TransformerSettings::class.java).let {
            it.example shouldBe "value"
        }
        applicationContext.getBean(${pascalCaseTransformerId}TransformerDefaultParameters::class.java).let {
            it.example2 shouldBe "value2"
            it.timeout shouldBe null
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