package ${package}.example

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants
import pl.beone.promena.transformer.contract.transformation.Transformation
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters
import ${package}.applicationmodel.${camelCaseTransformerId}Transformation

fun transform(): Transformation {
    // HTTP: localhost:8080
    // Repeat: 1
    // Concurrency: 1
    // Data: example.txt

    return ${camelCaseTransformerId}Transformation(MediaTypeConstants.TEXT_PLAIN, ${camelCaseTransformerId}Parameters("example"))
}