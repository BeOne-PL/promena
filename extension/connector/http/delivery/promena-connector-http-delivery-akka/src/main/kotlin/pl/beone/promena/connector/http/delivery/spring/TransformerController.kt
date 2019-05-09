package pl.beone.promena.connector.http.delivery.spring

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.server.ResponseStatusException
import pl.beone.promena.connector.http.contract.communication.FromMapToCommunicationParametersConverter
import pl.beone.promena.core.contract.transformation.TransformationUseCase
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerNotFoundException
import pl.beone.promena.transformer.applicationmodel.exception.transformer.TransformerTimeoutException

@Controller
class TransformerController {

    companion object {
        private val logger = LoggerFactory.getLogger(TransformerController::class.java)
    }

    @Autowired
    private lateinit var transformationUseCase: TransformationUseCase

    @Autowired
    private lateinit var fromMapToCommunicationParametersConverter: FromMapToCommunicationParametersConverter

    @PostMapping("/transform/{transformerId}")
    fun transfer(@PathVariable transformerId: String,
                 @RequestBody bytes: ByteArray,
                 @RequestParam parameters: Map<String, String>): HttpEntity<ByteArray> =
            try {
                HttpEntity(transformationUseCase.transform(transformerId,
                                                           bytes,
                                                           fromMapToCommunicationParametersConverter.convert(parameters)))
            } catch (e: Exception) {
                val status = when (e) {
                    is TransformerNotFoundException -> BAD_REQUEST
                    is TransformerTimeoutException  -> REQUEST_TIMEOUT
                    else                            -> INTERNAL_SERVER_ERROR
                }

                logger.error("An error occurred during processing <{}> <{}> request", transformerId, parameters, e)

                throw ResponseStatusException(status, e.message, e)
            }

}
