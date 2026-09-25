#!/bin/bash
set -e

# Custom entrypoint wrapper to run collation refresh before standard postgres entrypoint
# This ensures the refresh runs on container restart, not just initial setup

# Run our custom initialization scripts
if [ -f /var/lib/postgresql/data/PG_VERSION ]; then
    echo "Running custom initialization tasks..."
    for script in /docker-entrypoint-initdb.d/*; do
        if [ -f "$script" ] && [ -x "$script" ] && [[ "$script" == *.sh ]]; then
            echo "Running $script"
            "$script"
        fi
    done
fi

# Invoke the original postgres entrypoint
exec /usr/local/bin/docker-entrypoint.sh "$@"
