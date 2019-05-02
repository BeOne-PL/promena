package pl.beone.lib.dockertestrunner.external

import java.io.File

fun onDocker(): Boolean = File("/.dockerenv").exists()

fun runOnHost(toRun: () -> Unit) {
    if(!onDocker()) {
        toRun()
    }
}

fun runOnDocker(toRun: () -> Unit) {
    if(onDocker()) {
        toRun()
    }
}