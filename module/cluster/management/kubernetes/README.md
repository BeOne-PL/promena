# Promena module - `promena-cluster-management-kubernetes`

## Description
This module enables The Kubernetes API to discover peers and form an Akka Cluster (see [Akka Management](https://doc.akka.io/docs/akka-management/1.0.5/akka-management.html), [Akka Cluster Bootstrap - Kubernetes API](https://doc.akka.io/docs/akka-management/1.0.5/bootstrap/kubernetes-api.html) and [Akka Discovery Methods - Kubernetes API](https://doc.akka.io/docs/akka-management/1.0.5/discovery/kubernetes.html) for more details).

Additionally, it uses [akka-cluster-custom-downing](https://github.com/TanUkkii007/akka-cluster-custom-downing) to provide a configurable auto-downing strategy that you can choose based on your distributed application design.

Visit [Sample#Kubernetes & OpenShift](https://github.com/BeOne-PL/promena-sample#kubernetes-openshift) to find out how to set the deployment properties of `Kubernetes` and `OpenShift`.

## Dependency
```xml
<dependency>
    <groupId>pl.beone.promena.cluster.management</groupId>
    <artifactId>promena-cluster-management-kubernetes-configuration</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Properties
```properties
# See https://doc.akka.io/docs/akka-management/1.0.5/akka-management.html, https://doc.akka.io/docs/akka-management/1.0.5/bootstrap/kubernetes-api.html and https://doc.akka.io/docs/akka-management/1.0.5/discovery/kubernetes.html for more details
akka.remote.netty.tcp.hostname=${kubernetes.promena.pod.ip}

akka.management.http.hostname=${kubernetes.promena.pod.ip}
akka.management.http.bind-hostname=${kubernetes.promena.pod.ip}
akka.management.http.port=8558
akka.management.http.bind-port=8558

akka.management.cluster.bootstrap.contact-point-discovery.required-contact-point-nr=1
akka.management.cluster.bootstrap.contact-point-discovery.port-name=management

akka.discovery.method=kubernetes-api
akka.discovery.kubernetes-api.pod-namespace=${kubernetes.promena.namespace}
akka.discovery.kubernetes-api.pod-label-selector=app=${kubernetes.promena.label.app}

# See https://github.com/TanUkkii007/akka-cluster-custom-downing for more details
akka.cluster.downing-provider-class=tanukki.akka.cluster.autodown.MajorityLeaderAutoDowning

custom-downing.stable-after=10s
custom-downing.majority-leader-auto-downing.majority-member-role=
custom-downing.majority-leader-auto-downing.down-if-in-minority=true
custom-downing.majority-leader-auto-downing.shutdown-actor-system-on-resolution=true
```