# Developers Guide

## Install Dependencies

You need the following dependencies for building and running the tests:

* Java Development Kit 11
* Maven 3.6.3 or later
* Docker

## Project Keeper

This project uses [Project Keeper](https://github.com/exasol/project-keeper) for managing changelog, build scripts etc.

After updating the version or changing dependencies do the following

1. Run `mvn project-keeper:fix`
2. Commit the modified files

The build will fail if you don't do this.

## Release Droid

This project uses [Release Droid](https://github.com/exasol/release-droid) for creating GitHub releases and publishing to Maven Central.

First install and configure Release Droid as described in the [user guide](https://github.com/exasol/release-droid/blob/main/doc/user_guide/user_guide.md).

To build releases, run the following command:

```sh
java -jar release-droid.jar -n exasol-jdbc-connection-factory -g release
```

## Publishing Java 8 JAR

To publish a JAR for Java 8 to Maven Central run the following commands:

1. Verify the build works with Java 11:
    ```sh
    mvn clean verify
    ```
2. Edit file `pk_generated_parent.pom`:
  * Update `java.version` to 8
  * Remove plugin `duplicate-finder-maven-plugin`
  * Remove plugin `error-code-crawler-maven-plugin`
3. Add the following to your `~/.m2/settings.xml`:
    ```xml
    <settings>
        <servers>
            <server>
                <id>ossrh</id>
                <username>user</username>
                <password>password</password>
            </server>
        </servers>
        <profiles>
            <profile>
                <id>ossrh</id>
                <activation>
                    <activeByDefault>true</activeByDefault>
                </activation>
                <properties>
                    <gpg.executable>gpg</gpg.executable>
                    <gpg.keyname>key</gpg.keyname>
                    <gpg.passphrase>password</gpg.passphrase>
                </properties>
            </profile>
        </profiles>
    </settings>
    ```
4. Run the following command:
    ```sh
    JAVA_HOME=$JAVA8_HOME mvn -Dgpg.skip=false -Dmaven.test.skip=true clean deploy
    ```
5. After some time the artifacts will be available on [Maven Central](https://repo1.maven.org/maven2/com/exasol/exasol-jdbc-connection-factory/).
