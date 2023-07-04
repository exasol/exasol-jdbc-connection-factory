# Jdbc Connection Factory 1.4.1, released 2023-07-04

Code name: long is long

## Summary

## Features

* #7: Integer overflow for update operations<br>
  `executeUpdate()` and `executedUpdatePrepared()` now call `executeLargeUpdate()` on JDBC
  level to match their `long` return type

## Dependency Updates

### Compile Dependency Updates

* Updated `com.exasol:exasol-jdbc:7.1.16` to `7.1.20`
* Updated `org.apache.maven:maven-artifact:3.8.6` to `3.9.3`

### Test Dependency Updates

* Updated `com.exasol:exasol-testcontainers:6.4.0` to `6.6.0`
* Removed `org.junit.jupiter:junit-jupiter-api:5.9.1`
* Added `org.junit.jupiter:junit-jupiter:5.9.3`
* Updated `org.mockito:mockito-junit-jupiter:4.9.0` to `5.4.0`

### Plugin Dependency Updates

* Updated `com.exasol:error-code-crawler-maven-plugin:1.2.1` to `1.2.3`
* Updated `com.exasol:project-keeper-maven-plugin:2.9.1` to `2.9.7`
* Updated `org.apache.maven.plugins:maven-compiler-plugin:3.10.1` to `3.11.0`
* Updated `org.apache.maven.plugins:maven-deploy-plugin:3.0.0` to `3.1.1`
* Updated `org.apache.maven.plugins:maven-enforcer-plugin:3.1.0` to `3.3.0`
* Updated `org.apache.maven.plugins:maven-failsafe-plugin:3.0.0-M7` to `3.0.0`
* Updated `org.apache.maven.plugins:maven-javadoc-plugin:3.4.1` to `3.5.0`
* Updated `org.apache.maven.plugins:maven-surefire-plugin:3.0.0-M7` to `3.0.0`
* Added `org.basepom.maven:duplicate-finder-maven-plugin:1.5.1`
* Updated `org.codehaus.mojo:flatten-maven-plugin:1.3.0` to `1.4.1`
* Updated `org.codehaus.mojo:versions-maven-plugin:2.13.0` to `2.15.0`
* Updated `org.jacoco:jacoco-maven-plugin:0.8.8` to `0.8.9`
