package pl.beone.promena.communication.file.common.extension

import java.net.URI

fun URI.isTheSame(location: URI): Boolean =
    this == location

fun URI.isSubPath(location: URI): Boolean =
    toString().startsWith(location.toString())

fun URI.notIncludedIn(location: URI): Boolean =
    !this.isTheSame(location) || !this.isSubPath(location)