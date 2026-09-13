#!/usr/bin/env bash
# Convenience script to run the packaged jar, locally or on the EC2 instance.
# The listening port is configurable through the PORT environment variable
# (or a positional argument), matching NetworkingServer#resolvePort.
#
# Usage:
#   ./run.sh            # listens on 35000 (default)
#   ./run.sh 8080        # listens on 8080
#   PORT=8080 ./run.sh   # equivalent, via environment variable
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR_PATH="${JAR_PATH:-$SCRIPT_DIR/../target/networking-lab-1.0.0.jar}"
export PORT="${1:-${PORT:-35000}}"

echo "Starting networking-lab on port $PORT using $JAR_PATH"
exec java -jar "$JAR_PATH"
