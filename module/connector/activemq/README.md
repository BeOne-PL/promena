# Promena connector module - `promena-connector-activemq`

## Description
This connector module provides the possibility to transfer data using ActiveMQ based on serialization.

It gets only messages for the transformers that are included in Promena.  

TODO
Visit [Sample#Alfresco/Connector](https://github.com/BeOne-PL/promena-sample#connector) and [Sample#Alfresco/Transformer & Rendition](https://github.com/BeOne-PL/promena-sample#transformer--rendition) to find out how to set the deployment properties of this connector module.

## Flow
1. Serialize [`TransformationDescriptor`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/transformation/TransformationDescriptor.kt). If you program in a JVM language, you can use [`KryoSerializationService`](./../../../base/promena-core/internal/internal-serialization/src/main/kotlin/pl/beone/promena/core/internal/serialization/KryoSerializationService.kt) or [`ClassLoaderKryoSerializationService`](./../../../base/promena-core/internal/internal-serialization/src/main/kotlin/pl/beone/promena/core/internal/serialization/ClassLoaderKryoSerializationService.kt) from the following dependency:
```xml
<dependency>
    <groupId>pl.beone.promena.base</groupId>
    <artifactId>promena-core-internal-serialization</artifactId>
    <version>1.0.0</version>
</dependency>
```
2. Send the request message to `${promena.connector.activemq.consumer.queue.request}` queue:
    * The body with the serialized data
    * `serialization_class` property set to `pl.beone.promena.core.applicationmodel.transformation.TransformationDescriptor`
3. Remember `correlationId`, in order to associate a response message with the request message 
4. The response message contains `promena_transformation_timestamp_start` and `promena_transformation_timestamp_end` properties with the execution timestamp and it may appear in:
    * `promena.connector.activemq.consumer.queue.response` queue - deserialize the body to [`PerformedTransformationDescriptor`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/transformation/PerformedTransformationDescriptor.kt)
    * `promena.connector.activemq.consumer.queue.response.error` queue - deserialize the body to the class from `serialization-class` property - it will be a subclass of `Throwable`

## Client implementation
See [`TransformerSender`](https://github.com/BeOne-PL/promena-alfresco/blob/master/connector/alfresco-promena-connector-activemq/src/main/kotlin/pl/beone/promena/alfresco/module/connector/activemq/delivery/activemq/TransformerSender.kt) and [`TransformerResponseConsumer`](https://github.com/BeOne-PL/promena-alfresco/blob/master/connector/alfresco-promena-connector-activemq/src/main/kotlin/pl/beone/promena/alfresco/module/connector/activemq/delivery/activemq/TransformerResponseConsumer.kt) example implementation.

The implementation for Alfresco is available (see [`alfresco-promena-activemq`](https://github.com/BeOne-PL/promena-alfresco/tree/master/connector/alfresco-promena-connector-activemq) for more details).

## Dependency
```xml
<dependency>
    <groupId>pl.beone.promena.connector</groupId>
    <artifactId>promena-connector-activemq-configuration</artifactId>
    <version>1.0.1</version>
</dependency>
```

## Properties
```properties
promena.connector.activemq.consumer.queue.request=Promena.request
promena.connector.activemq.consumer.queue.response=Promena.response
promena.connector.activemq.consumer.queue.response.error=Promena.response.error
```
This connector module uses standard Spring Boot properties:
* [Spring Appendix - Integration properties](https://docs.spring.io/spring-boot/docs/2.2.1.RELEASE/reference/html/appendix-application-properties.html#integration-properties)