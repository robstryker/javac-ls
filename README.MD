# Javac Language Server

## Summary

This is a lightweight Java language server based on javac, designed for AI-centric development workflows. It provides a minimal API for testing and will be extended with javac-based language features.

The protocol is based on LSP4J, providing a simplified interface for language server communication.

The base protocol of LSP can be found [here](https://microsoft.github.io/language-server-protocol/specification).

## Install

This client / server code can be found at: https://github.com/nb-javac-shaded/javac-ls

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/),
and then clone your fork:

    $ git clone git@github.com:<you>/javac-ls
    $ cd javac-ls
    $ git remote add upstream https://github.com/nb-javac-shaded/javac-ls

At any time, you can pull changes from the upstream and merge them onto your main branch:

    $ git checkout main                 # switches to the 'main' branch
    $ git pull upstream main            # fetches all 'upstream' changes and merges 'upstream/main' onto your 'main' branch
    $ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'main' branch in-sync with the
'upstream/main'.

## Building The client and server

This command will run the build:

    $ ./build.sh

Or with tests:

    $ ./build-with-tests.sh

If you prefer to use Maven directly:

    $ MAVEN_OPTS="-Djdk.xml.maxGeneralEntitySizeLimit=0 -Djdk.xml.totalEntitySizeLimit=0 -Djdk.xml.entityExpansionLimit=0" mvn clean verify

To skip tests:

    $ ./build.sh -DskipTests=true

But *do not* push changes without having the new and existing unit tests pass!

## Running the Server

After building, a distribution ZIP is created at `distribution/distribution/target/org.jboss.tools.javac.ls.distribution-0.0.1-SNAPSHOT.zip`

Extract and run:

    $ unzip distribution/distribution/target/org.jboss.tools.javac.ls.distribution-0.0.1-SNAPSHOT.zip
    $ cd rsp-distribution
    $ java -jar bin/felix.jar

### Configuration

The server can be configured via system properties:

- **`javacls.workspace.path`** - Path to the workspace directory where server data is stored
  - Default: `~/.javacls/workspace`
  - Example: `-Djavacls.workspace.path=/path/to/my/workspace`

- **`javacls.server.port`** - Port number for the language server
  - Default: `27511`
  - Example: `-Djavacls.server.port=8080`

Example with custom workspace:

    $ java -Djavacls.workspace.path=/my/project/workspace -jar bin/felix.jar

## Current Status

This is a minimal setup with:
- API with ping/statusChanged methods
- Basic client CLI for testing
- Framework bundles for launching and logging
- Distribution as a standalone OSGi application

The server implementation and full language server features are under development.
