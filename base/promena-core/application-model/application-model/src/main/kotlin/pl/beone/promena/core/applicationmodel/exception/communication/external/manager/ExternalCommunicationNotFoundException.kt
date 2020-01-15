package pl.beone.promena.core.applicationmodel.exception.communication.external.manager

/**
 * Signals that there is no *external communication* that meets requirements.
 */
class ExternalCommunicationNotFoundException(
    message: String
) : NoSuchElementException(message)