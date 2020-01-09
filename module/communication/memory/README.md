# Promena communication module - `promena-communication-memory`

## Description
This communication module provides implementation based on memory for internal and external communication. Data is shared using memory. 

Visit [Sample#Alfresco/Communication/Memory](https://gitlab.office.beone.pl/promena/promena-sample#memory) and [Sample#Alfresco/Connector](https://gitlab.office.beone.pl/promena/promena-sample#connector) to find out how to set the deployment properties of this communication module.

## Dependency
### Internal
```xml
<dependency>
    <groupId>pl.beone.promena.communication.memory.internal</groupId>
    <artifactId>promena-communication-memory-internal-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

### External
```xml
<dependency>
    <groupId>pl.beone.promena.communication.memory.external</groupId>
    <artifactId>promena-communication-memory-external-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```