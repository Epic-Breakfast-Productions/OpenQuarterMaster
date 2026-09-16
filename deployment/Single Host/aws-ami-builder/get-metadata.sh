#!/bin/bash

BASE_URL="http://169.254.169.254/latest/meta-data/"

OUTPUT_FILE="/tmp/instance_metadata.txt"

> "$OUTPUT_FILE"

declare -a KEYS=("instance-id" "instance-type" "ami-id"
                 "placement/availability-zone" "placement/region"
                 "public-ipv4" "local-ipv4" "public-hostname"
                 "local-hostname" "mac" "security-groups")


for key in "${KEYS[@]}"; do
    echo -n "${key}: " >> "$OUTPUT_FILE"
    RESPONSE=$(curl -s --write-out "%{http_code}" --output temp.txt "${BASE_URL}${key}")
    if [ "$RESPONSE" -eq 200 ]; then
        cat temp.txt >> "$OUTPUT_FILE"
    else
        echo "Error retrieving ${key}, HTTP Status: $RESPONSE" >> "$OUTPUT_FILE"
    fi
    echo >> "$OUTPUT_FILE"
done
rm temp.txt

echo "Metadata saved to $OUTPUT_FILE" && cat $OUTPUT_FILE