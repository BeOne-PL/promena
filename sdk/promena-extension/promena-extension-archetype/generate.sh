#!/usr/bin/env bash
mvn archetype:generate -B -DarchetypeGroupId=pl.beone.promena.sdk.maven.archetype \
    -DarchetypeArtifactId=promena-extension-archetype \
    -DgroupId=pl.beone.promena.extension \
    -Dpackage=pl.beone.promena.extension \
    -DartifactId=extension-example \
    -Dversion=1.0.0