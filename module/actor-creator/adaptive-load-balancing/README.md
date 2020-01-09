# Promena module - `promena-actor-creator-adaptive-load-balancing`

## Description
This module provides implementation [`AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator`](./external-akka/src/main/kotlin/pl/beone/promena/actorcreator/adaptiveloadbalancing/configuration/external/akka/actor/config/AdaptiveLoadBalancingGroupOnSmallestMailboxPoolActorCreator.kt) of [`ActorCreator`](./../../../base/promena-core/application-model/application-model/src/main/kotlin/pl/beone/promena/core/applicationmodel/transformation/PerformedTransformationDescriptor.kt). It works in two stages:
* Cluster level - performs load balancing of messages to cluster nodes based on the cluster metrics data (see [Cluster Metrics#Adaptive Load Balancing](https://doc.akka.io/docs/akka/2.5.26/cluster-metrics.html#adaptive-load-balancing) for more details)
* Local level - tries to send to the non-suspended routee with fewest messages in mailbox

## Dependency
```xml
<dependency>
    <groupId>pl.beone.promena.actor-creator</groupId>
    <artifactId>promena-actor-creator-adaptive-load-balancing-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Properties
```properties
# MetricsSelector to produce the probabilities, a.k.a. weights
actor-creator.adaptive-load-balancing.metrics-selector=akka.cluster.metrics.MixMetricsSelector::getInstance
```