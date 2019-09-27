package ${package}.example

import pl.beone.promena.transformer.applicationmodel.mediatype.MediaTypeConstants.TEXT_PLAIN
import pl.beone.promena.transformer.contract.transformation.Transformation
import ${package}.applicationmodel.${camelCaseTransformerId}Parameters
import ${package}.applicationmodel.${camelCaseTransformerId}Transformation

fun promena(): Transformation {
    // Data: example.txt

    return ${camelCaseTransformerId}Transformation(TEXT_PLAIN, ${camelCaseTransformerId}Parameters("example"))
}