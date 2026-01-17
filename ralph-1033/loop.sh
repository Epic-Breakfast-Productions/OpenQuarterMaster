#!/bin/bash

# Ralph Loop Script - Issue #1033 Investigation
# Usage:
#   ./loop.sh              # Build mode, unlimited iterations
#   ./loop.sh 20           # Build mode, max 20 iterations
#   ./loop.sh plan         # Plan mode, unlimited
#   ./loop.sh plan 5       # Plan mode, max 5 iterations

set -euo pipefail

# Navigate to repo root (parent of ralph-1033)
cd "$(dirname "$0")/.."

# Determine mode and iteration limit
if [[ "${1:-}" == "plan" ]]; then
    MODE="plan"
    PROMPT_FILE="ralph-1033/PROMPT_plan.md"
    MAX_ITERATIONS="${2:-0}"
else
    MODE="build"
    PROMPT_FILE="ralph-1033/PROMPT_build.md"
    MAX_ITERATIONS="${1:-0}"
fi

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)

echo "Starting Ralph in $MODE mode"
echo "Using prompt: $PROMPT_FILE"
echo "Max iterations: ${MAX_ITERATIONS:-unlimited}"
echo "Branch: $CURRENT_BRANCH"
echo ""

iteration=0

while true; do
    iteration=$((iteration + 1))
    echo ""
    echo "=========================================="
    echo "Iteration $iteration - $(date)"
    echo "=========================================="

    # Run Claude with the prompt
    cat "$PROMPT_FILE" | claude -p \
        --dangerously-skip-permissions \
        --output-format=stream-json \
        --model opus \
        --verbose

    # Push changes
    git push origin "$CURRENT_BRANCH" 2>/dev/null || \
        git push -u origin "$CURRENT_BRANCH"

    # Check iteration limit
    if [[ "$MAX_ITERATIONS" -gt 0 ]] && [[ "$iteration" -ge "$MAX_ITERATIONS" ]]; then
        echo "Reached max iterations ($MAX_ITERATIONS). Stopping."
        break
    fi

    echo "Loop complete. Starting next iteration..."
    sleep 2
done
