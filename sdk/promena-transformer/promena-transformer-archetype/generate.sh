#!/usr/bin/env bash
mvn archetype:generate -B -DarchetypeGroupId=pl.beone.promena.sdk.maven.archetype \
    -DarchetypeArtifactId=promena-transformer-archetype \
    -DgroupId=pl.beone.promena.transformer \
    -Dpackage=pl.beone.promena.transformer.test.kotlin \
    -DartifactId=test-kotlin \
    -Dversion=1.0.0 \
    -DtransformerName=report \
    -DtransformerSubName=memory-jasper-report