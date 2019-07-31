package pl.beone.promena.maven.plugin.docker

import com.github.dockerjava.api.model.BuildResponseItem
import com.github.dockerjava.core.command.BuildImageResultCallback
import org.apache.maven.plugin.logging.Log

internal class LoggerBuildImageResultCallback(private val log: Log) : BuildImageResultCallback() {

    override fun onNext(item: BuildResponseItem) {
        super.onNext(item)
        log.info(item.stream?.trimEnd('\n'))
    }
}