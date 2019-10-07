package pl.beone.promena.connector.normal.http

object PromenaNormalHttpHeaders {

    // Transformation
    const val TRANSFORMATION_PREFIX = "transformation{NUMBER}-"
    @JvmField
    val TRANSFORMATION_PREFIX_REGEX = """^transformation\d*-""".toRegex()

    const val TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX = "transformerId-name"
    const val TRANSFORMATION_TRANSFORMER_ID_NAME = "$TRANSFORMATION_PREFIX$TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX"
    @JvmField
    val TRANSFORMATION_TRANSFORMER_ID_NAME_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_TRANSFORMER_ID_NAME_SUFFIX".toRegex()

    const val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX = "transformerId-subName"
    const val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME = "$TRANSFORMATION_PREFIX$TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX"
    @JvmField
    val TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_TRANSFORMER_ID_SUB_NAME_SUFFIX".toRegex()

    const val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX = "mediaType-mimeType"
    const val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE = "$TRANSFORMATION_PREFIX$TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX"
    @JvmField
    val TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_MEDIA_TYPE_MIME_TYPE_SUFFIX".toRegex()

    const val TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX = "mediaType-charset"
    const val TRANSFORMATION_MEDIA_TYPE_CHARSET = "$TRANSFORMATION_PREFIX$TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX"
    @JvmField
    val TRANSFORMATION_MEDIA_TYPE_CHARSET_REGEX = "$TRANSFORMATION_PREFIX_REGEX$TRANSFORMATION_MEDIA_TYPE_CHARSET_SUFFIX".toRegex()

    @JvmStatic
    fun createTransformationTransformerIdNameHeader(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_TRANSFORMER_ID_NAME, ordinalNumber)

    @JvmStatic
    fun createTransformationTransformerIdSubNameHeader(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_TRANSFORMER_ID_SUB_NAME, ordinalNumber)

    @JvmStatic
    fun createTransformationMediaTypeMimeType(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_MEDIA_TYPE_MIME_TYPE, ordinalNumber)

    @JvmStatic
    fun createTransformationMediaTypeCharset(ordinalNumber: Int): String =
        replaceNumberPlaceholder(TRANSFORMATION_MEDIA_TYPE_CHARSET, ordinalNumber)

    @JvmStatic
    private fun replaceNumberPlaceholder(text: String, ordinalNumber: Int): String =
        text.replace("{NUMBER}", if (ordinalNumber == 1) "" else ordinalNumber.toString())

    // DataDescriptor
    const val DATA_DESCRIPTOR_MEDIA_TYPE_MIME_TYPE = "dataDescriptor-mediaType-mimeType"
    const val DATA_DESCRIPTOR_MEDIA_TYPE_CHARSET = "dataDescriptor-mediaType-charset"
}