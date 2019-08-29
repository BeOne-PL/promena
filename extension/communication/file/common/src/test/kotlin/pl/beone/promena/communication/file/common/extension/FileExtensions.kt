package pl.beone.promena.communication.file.common.extension

import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File

internal fun FileData.exists(): Boolean =
    File(getLocation()).exists()