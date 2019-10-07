package pl.beone.promena.connector.normal.http

object PromenaNormalHttpHeaders {

    // Transformation
    val TRANSFORMATION_PREFIX = "transformation{NUMBER}-"
    val TRANSFORMATION_PREFIX_REGEX = """^transformation\d*-""".toRegex()

    const val TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX = "transformerId-name"
    val TRANSFORMATION_TRANSFORMER_ID_NAME = "$TRANSFORMATION_PREFIX$TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX"
    val TRANSFORMATION_TRANSFORMER_ID_NAME_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX".toRegex()

    const val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX = "transformerId-subName"
    val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME = "$TRANSFORMATION_PREFIX$TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX"
    val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX".toRegex()

    const val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX = "mediaType-mimeType"
    val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE = "$TRANSFORMATION_PREFIX$TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX"
    val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX".toRegex()

    const val TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX = "mediaType-charset"
    val TRANSFORMATION_MEDIA_TYPE_CHARSET = "$TRANSFORMATION_PREFIX$TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX"
    val TRANSFORMATION_MEDIA_TYPE_CHARSET_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX".toRegex()

    fun createTransformationTransformerIdNameHeader(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_TRANSFORMER_ID_NAME, ordinalNumber)

    fun createTransformationTransformerIdSubNameHeader(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_TRANSFORMER_ID_SUB_NAME, ordinalNumber)

    fun createTransformationMediaTypeMimeType(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_MEDIA_TYPE_MIME_TYPE, ordinalNumber)

    fun createTransformationMediaTypeCharset(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_MEDIA_TYPE_CHARSET, ordinalNumber)

    private fun replaceNumberPlaceholder(text: String, ordinalNumber: Int): String =
        text.replace("{NUMBER}", if (ordinalNumber == 1) "" else ordinalNumber.toString())

    // DataDescriptor
    const val DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE = "dataDescriptor-mediaType-mimeType"
    const val DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET = "dataDescriptor-mediaType-charset"
}