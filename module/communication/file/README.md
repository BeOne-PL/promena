# Promena communication module - `promena-communication-file`

## Description
This communication module provides implementation based on file for internal and external communication. Data is shared using files.

**It is very important to choose a path that is available on every Promena node in the same location.**

Visit [Sample#Deployment/Manual](https://github.com/BeOne-PL/promena-sample#manual) and [Sample#Alfresco/Communication/File](https://github.com/BeOne-PL/promena-sample#file) to find out how to set the deployment properties of this communication module and how to integrate with an application.

## Dependency
### Internal
```xml
<dependency>
    <groupId>pl.beone.promena.communication.file.internal</groupId>
    <artifactId>promena-communication-file-internal-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

### External
```xml
<dependency>
    <groupId>pl.beone.promena.communication.file.external</groupId>
    <artifactId>promena-communication-file-external-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Properties
```properties
# Directory where data is persisted and shared across Promena nodes
communication.file.internal.directory.path=/tmp
```