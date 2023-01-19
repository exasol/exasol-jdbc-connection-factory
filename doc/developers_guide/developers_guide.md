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
