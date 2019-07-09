package ${package}

import pl.beone.promena.transformer.contract.model.Data
import pl.beone.promena.transformer.internal.model.data.FileData
import java.io.File

internal class FileDataProcessor {

    fun transform(data: Data): FileData =
            data.getFile()
                    .addHashAtTheEnd()
                    .toFileData()

    private fun Data.getFile(): File =
            File(this.getLocation())

    private fun File.addHashAtTheEnd(): File =
            apply {
                appendText("#")
            }

    private fun File.toFileData(): FileData =
            FileData(toURI())
}