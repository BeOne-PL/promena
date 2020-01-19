# Promena Development Guide
## Transformer
Visit [Promena#Transformer](./README.md#transformer) to find out the role of transformers and to see the list of production-ready implementations. 

Visit [Promena Sample#Transformer](https://gitlab.office.beone.pl/promena/promena-sample#transformer) to see example implementations of transformers. 

### Base project
A base project of Promena transformer you can generate by executing: 
```
mvn archetype:generate -B \
    -DarchetypeGroupId=pl.beone.promena.sdk.maven.archetype \
    -DarchetypeArtifactId=promena-transformer-archetype \
    -DarchetypeVersion=1.0.0 \
    -DgroupId=<group id> \
    -Dpackage=<package> \
    -DartifactId=<artifact id> \
    -Dversion=<version> \
    -DtransformerName=<transformer name> \
    -DtransformerSubName=<transformer sub name>
```

The generated project contains 4 folders that are modules.

Let's assume that `<cc transfomer id>` is the camel case constant created from `<transformer name><transformer sub name>` (signs that aren't word characters are removed and the next character is converted to uppercase).

#### `<artifact id>-application-model` (`application-model` folder)
Contains domain classes. It can be used to get information about the transformer. It also allows to check if the transformer supports given media type and parameters before sending a transformation (`<cc transfomer id>Support.kt`). 
* `<cc transfomer id>Constants.kt` - contains *name* and *sub name* of the transformer
* `<cc transfomer id>Dsl.kt` - helps to construct [`Transformation`](./base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/transformation/Transformation.kt) specific for the transformer
* `<cc transfomer id>ParametersConstants.kt` - contains parameters (names and class types) specific for the transformer
* `<cc transfomer id>ParametersDsl.kt` - helps to construct [`Parameters`](./base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/model/Parameters.kt) specific for the transformer
* `<cc transfomer id>Support.kt` - checks if the transformer supports transformation parameters (`isSupported` function). It also contains separate functions to check if a target media type (`<cc transfomer id>Support.MediaTypeSupport.isSupported`) and parameters (`<cc transfomer id>Support.ParametersSupport.isSupported`) are supported

#### `<artifact id>` (`implementation` folder)
Provides the transformer implementation.
* `<cc transfomer id>Transformer.kt` - performs a transformation. It implements [`Transformer`](./base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/Transformer.kt) interface that is essential for transformers (input for a transformation)
* `<cc transfomer id>DefaultParameters.kt` - contains parameters from `<cc transfomer id>ParametersConstants.kt` for which default values are possible to set
* `<cc transfomer id>Settings.kt` - contains settings of the transformer. Settings should contain constant values, for example, HTTP address, HTTP port, username, password, etc.

#### `<artifact id>-configuration` (`configuration` folder)
Creates Spring beans. This module has to be included in Promena as dependency to register the transformer. 
* `<cc transfomer id>TransformerModuleConfig.kt` - registers component scans on `<package>.configuration` and adds properties from `transformer-<artifact id>.properties`. This class is located in package `pl.beone.promena.configuration`. It's required because Promena scans beans located in `pl.beone.promena` by default.
* `<cc transfomer id>TransformerConfigurationContext.kt` - creates beans of `<cc transfomer id>DefaultParameters.kt` and `<cc transfomer id>Settings.kt`
* `<cc transfomer id>TransformerContext.kt` - creates the bean of `<cc transfomer id>Transformer.kt`
* `<cc transfomer id>TransformerLogger.kt` - logs information about the transformer (its default parameters and default settings) on startup

#### `<artifact id>-example` (folder `example`)
Contains examples of [`Transformation`](./base/promena-transformer/contract/src/main/kotlin/pl/beone/promena/transformer/contract/transformation/Transformation.kt) (`<cc transfomer id>Example.kt`) specific for this transformer. 

These examples can be run on Promena using [Promena IntelliJ plugin](./README.md#intellij-plugin).

### Test
If [`DockerExtension`](https://gitlab.office.beone.pl/library/docker-extension-junit5/blob/master/src/main/kotlin/pl/beone/lib/junit/jupiter/external/DockerExtension.kt) is used, tests are run in Docker container on Docker image created from `implementation/src/docker/Dockerfile` and `implementation/src/main/resources/docker/Dockerfile-fragment` files.
  
Visit [DockerExtension JUnit5](https://gitlab.office.beone.pl/library/docker-extension-junit5) to find out how it works.
 
See examples of tests in the generated project (`implementation` folder).

## Module
Visit [Promena#Module](./README.md#module) to find out the role of modules and to see the list of production-ready implementations of transformers. 

Visit [Promena Sample#Module](https://gitlab.office.beone.pl/promena/promena-sample#module) to see example implementations of modules. 

### Base project
A base project of Promena module you can generate by executing: 
```
mvn archetype:generate -B \
    -DarchetypeGroupId=pl.beone.promena.sdk.maven.archetype \
    -DarchetypeArtifactId=promena-module-archetype \
    -DarchetypeVersion=1.0.0 \
    -DgroupId=<group id> \
    -Dpackage=<package> \
    -DartifactId=<artifact id> \
    -Dversion=<version>
```

The generated project contains 2 folders that are modules.

Let's assume that `<cc artifact id>` is the camel case constant created from `<artifact id>` (signs that aren't word characters are removed and the next character is converted to uppercase).

#### `<artifact id>-configuration` (`configuration` folder)
Creates Spring beans. This module has to be included in Promena as dependency to register the module. The root configuration class `<cc artifact id>ModuleConfig.kt` registers component scans on `<package>.configuration` and adds properties from `module-<artifact id>.properties`. This class is located in package `pl.beone.promena.configuration`. It's required because Promena scans beans located in `pl.beone.promena` by default.
 
#### `<artifact id>` (`implementation` folder) 
The module implementation should be placed here.

## Communication
Communication module is a special version of module.

Visit [Promena#Modele/Communication](./README.md#communication) to find out the role of communication modules and to see the list of production-ready implementations. 

### Internal communication
The following interfaces have to be implemented and registered as beans:
* [`InternalCommunicationConverter`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/internal/InternalCommunicationConverter.kt) 
* [`InternalCommunicationCleaner`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/internal/InternalCommunicationCleaner.kt)

### External communication
The following interfaces have to be implemented and registered as beans:
* [`IncomingExternalCommunicationConverter`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/external/IncomingExternalCommunicationConverter.kt)
* [`OutgoingExternalCommunicationConverter`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/external/OutgoingExternalCommunicationConverter.kt)

A instance of [`ExternalCommunication`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/external/manager/ExternalCommunication.kt) also has to be created and registered as a bean.

## Connector
Connector module is a special version of module.

Visit [Promena#Modele/Connector](./README.md#connector) to find out the role of connector modules and to see the list of production-ready implementations. 

[Promena#Flow/Promena](./README.md#promena) presents typical steps that a connector module has to make.
* steps 2-4 can be done using [`TransformationUseCase`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/transformation/TransformationUseCase.kt) ([`DefaultTransformationUseCase`](./base/promena-core/use-case/src/main/kotlin/pl/beone/promena/core/usecase/transformation/DefaultTransformationUseCase.kt) implementation, `defaultTransformationUseCase` bean name).
* steps 1 (deserialization) and 5 (serialization) can be done using [`SerializationService`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/serialization/SerializationService.kt) ([`AkkaSerializationService`](./base/promena-core/external/external-akka/src/main/kotlin/pl/beone/promena/core/external/akka/serialization/AkkaSerializationService.kt) implementation, `akkaSerializationService` bean name)

## Changing default implementations
By default, Promena registers the following implementations of the core interfaces:
* [`ExternalCommunicationManager`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/communication/external/manager/ExternalCommunicationManager.kt) ([`DefaultExternalCommunicationManager`](./base/promena-core/internal/internal/src/main/kotlin/pl/beone/promena/core/internal/communication/external/manager/DefaultExternalCommunicationManager.kt) implementation, `defaultExternalCommunicationManager` bean name)
* [`SerializationService`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/serialization/SerializationService.kt) ([`AkkaSerializationService`](./base/promena-core/external/external-akka/src/main/kotlin/pl/beone/promena/core/external/akka/serialization/AkkaSerializationService.kt) implementation, `akkaSerializationService` bean name)
* [`TransformationService`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/transformation/TransformationService.kt) ([`AkkaTransformationService`](./base/promena-core/external/external-akka/src/main/kotlin/pl/beone/promena/core/external/akka/transformation/AkkaTransformationService.kt) implementation, `akkaTransformationService` bean name)
* [`TransformationUseCase`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/transformation/TransformationUseCase.kt) ([`DefaultTransformationUseCase`](./base/promena-core/use-case/src/main/kotlin/pl/beone/promena/core/usecase/transformation/DefaultTransformationUseCase.kt) implementation, `defaultTransformationUseCase` bean name)
* [`TransformerConfig`](./base/promena-core/contract/contract/src/main/kotlin/pl/beone/promena/core/contract/transformer/config/TransformerConfig.kt) ([`PropertiesTransformerConfig`](./base/promena-core/external/external-spring/src/main/kotlin/pl/beone/promena/core/external/spring/transformer/config/PropertiesTransformerConfig.kt) implementation, `propertiesTransformerConfig` bean name)
* [`TransformerActorGetter`](./base/promena-core/contract/contract-akka/src/main/kotlin/pl/beone/promena/core/contract/actor/TransformerActorGetter.kt) ([`GroupedByNameTransformerActorGetter`](./base/promena-core/external/external-akka/src/main/kotlin/pl/beone/promena/core/external/akka/actor/GroupedByNameTransformerActorGetter.kt) implementation, `groupedByNameTransformerActorGetter` bean name)
* [`TransformersCreator`](./base/promena-core/contract/contract-akka/src/main/kotlin/pl/beone/promena/core/contract/transformer/config/TransformersCreator.kt) ([`GroupedByNameTransformersCreator`](./base/promena-core/external/external-akka/src/main/kotlin/pl/beone/promena/core/external/akka/transformer/config/GroupedByNameTransformersCreator.kt) implementation, `groupedByNameTransformersCreator` bean name)
* [`ActorCreator`](./base/promena-core/contract/contract-akka/src/main/kotlin/pl/beone/promena/core/contract/actor/config/ActorCreator.kt) (no default implementation)

All beans of these implementations are created using `@ConditionalOnMissingBean` annotation, so if you want to use your own implementation, include it and register as a bean in your own module.

It is also possible to provide custom:
* `ActorSystem`
* `ActorMaterializer`

