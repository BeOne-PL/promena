package pl.beone.promena.lib.http.client.applicationmodel.exception

class TransformationValidationException(message: String, exception: Exception? = null) : RuntimeException(message, exception)