package ${package}

import java.time.Duration

data class ${pascalCaseTransformerId}TransformerDefaultParameters(
    val optional: String? = null,
    val optionalLimitedValue: Int? = null,
    val timeout: Duration? = null
)