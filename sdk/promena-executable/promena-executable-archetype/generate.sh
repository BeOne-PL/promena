#!/usr/bin/env bash
mvn archetype:generate -B -DarchetypeGroupId=pl.beone.promena.sdk.maven.archetype \
    -DarchetypeArtifactId=promena-executable-archetype \
    -DgroupId=pl.beone.promena.executable.test \
    -DartifactId=promena-test-executable \
    -Dpackage=pl.beone.promena.executable.test \
    -Dversion=1.0.0