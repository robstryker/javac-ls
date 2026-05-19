#!/bin/sh
#
# Example script showing how to run the Javac-LS server with configuration
#

# Set workspace directory (optional - defaults to ~/.javacls/workspace)
WORKSPACE_PATH=/tmp/javacls-workspace

# Set server port (optional - defaults to 27511)
SERVER_PORT=27511

echo "Starting Javac-LS server..."
echo "  Workspace: $WORKSPACE_PATH"
echo "  Port: $SERVER_PORT"
echo ""

java -Djavacls.workspace.path=$WORKSPACE_PATH \
     -Djavacls.server.port=$SERVER_PORT \
     -jar bin/felix.jar
