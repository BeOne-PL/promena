# Promena connector module - `promena-connector-http`

## Description
This connector module provides the possibility to transfer data using HTTP based on serialization.

Visit [Sample#Deployment](https://gitlab.office.beone.pl/promena/promena-sample#communication), [Sample#Alfresco/Communication](https://gitlab.office.beone.pl/promena/promena-sample#communication) and [Sample#Alfresco/Rendition](https://gitlab.office.beone.pl/promena/promena-sample#rendition) to see find out how to set the deployment properties of this connector module.

## Flow
1. Serialize [`TransformationDescriptor`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/transformation/TransformationDescriptor.kt). If you program in a JVM language, you can use [`KryoSerializationService`](./../../../base/promena-core/internal/internal-serialization/src/main/kotlin/pl/beone/promena/core/internal/serialization/KryoSerializationService.kt) or [`ClassLoaderKryoSerializationService`](./../../../base/promena-core/internal/internal-serialization/src/main/kotlin/pl/beone/promena/core/internal/serialization/ClassLoaderKryoSerializationService.kt) from the following dependency:
```xml
<dependency>
    <groupId>pl.beone.promena.base</groupId>
    <artifactId>promena-core-internal-serialization</artifactId>
    <version>1.0.0</version>
</dependency>
```
2. Send `POST` request on `http://${server.address}:${server.port}/transform`:
    * The body with the serialized data
    * `Content-Type` header set to `application/octet-stream`
3. If the response status is:
    * `200` - deserialize the body to [`PerformedTransformationDescriptor`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/transformation/PerformedTransformationDescriptor.kt)
    * `500` - deserialize the body to the class from `serialization-class` header - it will be a subclass of `Throwable`

## Client implementation
See [`HttpPromenaTransformer`](./../../../lib/connector/http/src/main/kotlin/pl/beone/promena/lib/connector/http/external/HttpPromenaTransformer.kt) example implementation.

The implementation for Alfresco is available (see [`alfresco-promena-http`](https://gitlab.office.beone.pl/promena/promena-alfresco/tree/master/connector/alfresco-promena-connector-http) for more details).

## Dependency
```xml
<dependency>
    <groupId>pl.beone.promena.connector</groupId>
    <artifactId>promena-connector-http-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Properties
This connector module uses standard Spring Boot properties:
* [Spring Appendix - Web properties](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/html/appendix-application-properties.html#web-properties)
* [Spring Appendix - Server properties](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/html/appendix-application-properties.html#server-properties)