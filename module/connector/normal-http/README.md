# Promena connector module - `promena-connector-normal-http` (beta)

## Description
This connector module provides the possibility to transfer data using HTTP.

Normal HTTP connector is under development. It doesn't support:
* Passing [`Parameters`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/model/Parameters.kt) of [`Transformation`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/transformation/Transformation.kt)
* Passing [`Metadata`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/model/Metadata.kt) of [`DataDescriptor`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/data/DataDescriptor.kt)
* Passing external [`CommunicationParameters`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/communication/CommunicationParameters.kt)
* Returning more than 1 [`TransformedDataDescriptor`](./../../../base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/data/TransformedDataDescriptor.kt)

## Flow
Replace `{NUMBER}` by the number indexed from 1.

1. Send `multipart/form-data` `POST` request on `http://${server.address}:${server.port}/normal/transform`:
    * `transformation{NUMBER}-transformerId-name` header set to the name of a transformer
    * `transformation{NUMBER}-transformerId-subName` header set to the sub name of a transformer *(optional)*
    * `transformation{NUMBER}-mediaType-mimeType` header set to the name of a transformer target MIME type
    * `transformation{NUMBER}-mediaType-charset` header set to the name of a transformer target charset *(optional - UTF-8 by default)*
    * `Form-Data` structure:
        * The body with the data
        * `dataDescriptor-mediaType-mimetype` header set to the name of data MIME type
        * `dataDescriptor-mediaType-charset` header set to the name of data charset *(optional - UTF-8 by default)*
2. If the response status is:
    * `200` - the body with transformed data
    * `400` - in case of [`TransformationNotSupportedException`](./../../../base/promena-transformer/application-model/src/main/kotlin/pl/beone/promena/transformer/applicationmodel/exception/transformer/TransformationNotSupportedException.kt) and [`TransformerNotFoundException`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/exception/transformer/TransformerNotFoundException.kt)
    * `408` - in case of [`TransformerTimeoutException`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/exception/transformer/TransformerTimeoutException.kt)
    * `Another status` - standard Spring Boot exception handling
3. In case of an error a message with the following structure is returned (default Spring Boot error handling):
```json
{
  "timestamp": "2020-01-03T11:35:32.992+0000",
  "path": "/normal/transform",
  "status": 400,
  "error": "Bad Request",
  "message": "There is no <ocr> transformer",
  "requestId": "0849ad50"
}
```

## Dependency
```xml
<dependency>
    <groupId>pl.beone.promena.connector</groupId>
    <artifactId>promena-connector-normal-http-configuration</artifactId>
    <version>0.1.0</version>
</dependency>
```

## Properties
This connector module uses standard Spring Boot properties:
* [Spring Appendix - Web properties](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/html/appendix-application-properties.html#web-properties)
* [Spring Appendix - Server properties](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/html/appendix-application-properties.html#server-properties)