# Jdbc Factory for Exasol

[![Build Status](https://github.com/exasol/exasol-jdbc-connection-factory/actions/workflows/ci-build.yml/badge.svg)](https://github.com/exasol/exasol-jdbc-connection-factory/actions/workflows/ci-build.yml)
[![Maven Central &ndash; Jdbc Connection Factory](https://img.shields.io/maven-central/v/com.exasol/exasol-jdbc-connection-factory)](https://search.maven.org/artifact/com.exasol/exasol-jdbc-connection-factory)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=alert_status)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)

[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=security_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=reliability_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=sqale_index)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)

[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=code_smells)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=coverage)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=duplicated_lines_density)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=com.exasol%3Aexasol-jdbc-connection-factory&metric=ncloc)](https://sonarcloud.io/dashboard?id=com.exasol%3Aexasol-jdbc-connection-factory)

## Abstract
This module provides some functionality that is commonly required for projects that (interactively) connect to databases through JDBC.

## Service Classes

### JdbcConnectionFactory
This is a service class not specific to Exasol (except for its defaults and insistency to load the EXADriver).
It represents a pre-stage to a connection pool by supporting default values for the connection/authentication parameters and does cache credentials.

It also includes code to prompt an interactive user for credentials (database user password).

### ManagedConnection
This is a wrapper around a regular JDBC Connection, implementing the **AutoClosable** interface and also providing closure-support for iterating through automatically closed result sets.

Although not Exasol-specific in its usage, it also offers information on specific Exasol features and the release they came with. This can be used to customize generated Sql based on the connected database.

## Dependencies
The project depends on, and includes the *Exasol JDBC Driver 7.1.16*.

# Availability
The prepackaged artifact is available on Maven Central.

# Information for Developers

* [Developers Guide](doc/developers_guide/developers_guide.md)
* [Dependencies](dependencies.md)
* [Changelog](doc/changes/changelog.md)
