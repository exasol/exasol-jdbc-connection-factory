# Jdbc Factory for Exasol
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
The project depends on, and includes the *Exasol JDBC Driver 6.1.0*.

# Availablility
The prepackaged artifact is available on the internal Exasol Artifactory; just add the following parts to your **pom.xml**

```
    <repositories>
        <repository>
            <id>sandbox-releases</id>
            <url>http://maven.exasol.com/artifactory/exasol-sandbox-releases</url>
        </repository>
        <repository>
            <id>sandbox-snapshots</id>
            <url>http://maven.exasol.com/artifactory/exasol-sandbox-snapshots</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>com.exasol</groupId>
            <artifactId>jdbc-factory</artifactId>
            <scope>compile</scope>
            <version>1.0</version>
        </dependency>
    </dependencies>
```

