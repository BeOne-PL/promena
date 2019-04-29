package pl.beone.promena.util.cmdhttpclient.picocli

import picocli.CommandLine
import pl.beone.promena.transformer.applicationmodel.mediatype.MediaType

class MediaTypeConverter : CommandLine.ITypeConverter<MediaType> {

    override fun convert(value: String): MediaType =
            MediaType.create(value, Charsets.UTF_8)
}