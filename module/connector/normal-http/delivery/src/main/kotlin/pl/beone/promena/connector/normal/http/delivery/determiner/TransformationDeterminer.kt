package pl.beone.promena.connector.normal.http.delivery.determiner

import org.springframework.http.HttpHeaders
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.TRANSFORMATION_PREFIX_REGEX
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX
import pl.beone.promena.connector.normal.http.PromenaNormalHttpHeaders.TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX
import pl.beone.promena.transformer.applicationmodel.mediatype.mediaType
import pl.beone.promena.transformer.contract.transformation.Transformation
import pl.beone.promena.transformer.contract.transformation.singleTransformation
import pl.beone.promena.transformer.contract.transformation.transformation
import pl.beone.promena.transformer.contract.transformer.transformerId
import pl.beone.promena.transformer.internal.model.parameters.emptyParameters
import kotlin.text.Charsets.UTF_8

internal object TransformationDeterminer {

    private val numberRegEx = """\d+""".toRegex()

    /**
     * Groups `transformation{NUMBER}-transformerId-name`, `transformation{NUMBER}-transformerId-subName`,
     * `transformation{NUMBER}-mediaType-mimeType`, `transformation{NUMBER}-mediaType-charset` headers and creates [Transformation.Single].
     * Then, it creates [Transformation.Composite] from all of them in the order determined by `{NUMBER}`.
     */
    fun determine(headers: HttpHeaders): Transformation =
        headers.toSingleValueMap().toList()
            .filter { (key) -> TRANSFORMATION_PREFIX_REGEX.containsMatchIn(key) }
            .groupBy { (key) -> TRANSFORMATION_PREFIX_REGEX.find(key)?.value ?: error("Impossible. It's validated earlier") }
            .map { (transformationPrefix, transformationArguments) ->
                transformationPrefix to removeTransformationPrefix(transformationPrefix, transformationArguments)
            }
            .also { transformationHeaders -> validate(transformationHeaders.size) }
            .sortedBy { (transformationPrefix) -> extractTransformationOrdinalNumber(transformationPrefix) }
            .map { (transformationPrefix, arguments) -> determineTransformation(transformationPrefix, arguments) }
            .let(::transformation)

    private fun removeTransformationPrefix(prefix: String, arguments: List<Pair<String, String>>): Map<String, String> =
        arguments.map { (key, value) -> key.removePrefix(prefix) to value }.toMap()

    private fun validate(transformationHeadersSize: Int) {
        check(transformationHeadersSize > 0) { "There are no <transformation> group headers" }
    }

    private fun extractTransformationOrdinalNumber(key: String): Int =
        numberRegEx.find(key)?.value?.toInt() ?: 1

    private fun determineTransformation(transformationPrefix: String, arguments: Map<String, String>): Transformation.Single {
        val name = arguments[TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX]
            ?: error("There is no header <$transformationPrefix$TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX>")
        val subName = arguments[TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX]

        val mimeType = arguments[TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX]
            ?: error("There is no header <$transformationPrefix$TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX>")
        val charset = arguments[TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX] ?: UTF_8.name()

        return singleTransformation(transformerId(name, subName), mediaType(mimeType, charset), emptyParameters())
    }
}