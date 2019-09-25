package pl.beone.promena.intellij.plugin.parser

import pl.beone.promena.transformer.contract.data.DataDescriptor
import java.io.File

internal data class DataDescriptorWithFile(
    val dataDescriptor: DataDescriptor.Single,
    val file: File
)