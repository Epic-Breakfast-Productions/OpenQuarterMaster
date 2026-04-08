#!/bin/bash
set -e

# Refresh collation versions for all non-template databases
# This fixes collation mismatches when glibc library is updated during PostgreSQL minor version upgrades
# Only run if not a new init (i.e., on container restart/upgrade)
if [ -f /var/lib/postgresql/data/PG_VERSION ]; then
    echo "Checking and refreshing collation versions..."
    
    # Get list of databases that need collation refresh
    su postgres -c "psql -d postgres -t -c \"SELECT datname FROM pg_database WHERE datname NOT IN ('template0','template1') AND EXISTS (SELECT 1 FROM pg_collation WHERE collversion <> pg_collation_actual_version COLLATE pg_catalog.default)\" 2>/dev/null" > /tmp/needs_refresh.txt || true

    # Run REFRESH for each DB that needs it
    while read -r db; do
        # Skip empty lines
        if [ -z "$(echo "$db" | xargs)" ]; then
            continue
        fi
        echo "Refreshing collation for database: $db"
        su postgres -c "psql -d $db -c 'ALTER DATABASE $db REFRESH COLLATION VERSION'"
    done < /tmp/needs_refresh.txt
    
    rm -f /tmp/needs_refresh.txt
fi
