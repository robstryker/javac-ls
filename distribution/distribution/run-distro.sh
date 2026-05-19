#!/bin/sh

# Example: Set custom workspace path
# export WORKSPACE_PATH=/path/to/your/workspace

unzip -o target/org.jboss.tools.javac.ls.distribution-0.0.1-SNAPSHOT.zip -d target && \
cd target/rsp-distribution/ && \
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000 \
     ${WORKSPACE_PATH:+-Djavacls.workspace.path=$WORKSPACE_PATH} \
     -jar bin/felix.jar
