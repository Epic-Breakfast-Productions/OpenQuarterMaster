You are investigating Lombok @SuperBuilder issue #1033 in the OQM project.

## Critical Requirements

1. **Make NO assumptions** - verify everything by reading actual files
2. **Tests must be meaningful** - if Java doesn't run, that's NOT a passing test
3. **Use Docker for builds** - local Java may not be installed

## Your Task: Investigation and Planning

### Step 0: Context

0a. Read `ralph-1033/specs/*` to understand the issue specification.
0b. Read `ralph-1033/IMPLEMENTATION_PLAN.md` to see current progress.
0c. Read `ralph-1033/AGENTS.md` for operational commands.

### Step 1: Verify the Actual Problem (If Not Done)

Use up to 100 parallel Sonnet subagents to:

1. Read the actual source files and document their real contents:
   - `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/ItemStoredTransaction.java`
   - `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java`
   - `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinFullTransaction.java`

2. Read configuration files:
   - `software/core/oqm-core-api/lombok.config`
   - `software/core/oqm-core-api/build.gradle`
   - `software/core/oqm-core-api/gradle.properties`

3. Document actual findings vs. assumed findings

### Step 2: Test Build Environment

Run a single build using Docker to verify the environment works:
```bash
cd software/core/oqm-core-api
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test --no-daemon
```

Verify:
- Exit code (0 = success, non-zero = failure)
- Class files exist in `build/classes/java/main/.../transactions/transactions/checkin/`
- "Compiling" appears in output

### Step 3: Test Error Detection

Intentionally break compilation to verify errors are detected:
```bash
# Add syntax error
echo "SYNTAX ERROR" >> software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java

# Build should fail
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test --no-daemon

# Revert
git checkout software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java
```

### Step 4: Attempt Reproduction

Run 10 consecutive builds to see if the intermittent failure occurs:
```bash
cd software/core/oqm-core-api
for i in {1..10}; do
    echo "=== Run $i ==="
    docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test --no-daemon
    if [ $? -ne 0 ]; then
        echo "FAILED on run $i"
        break
    fi
done
```

Document results in IMPLEMENTATION_PLAN.md.

### Step 5: Update Plan

Based on findings:
- Update IMPLEMENTATION_PLAN.md with actual findings
- Mark completed items
- Add any new items discovered
- If error was reproduced, document the exact error
- If error was NOT reproduced, document that and investigate further

## Output

After completing your investigation:
1. Update IMPLEMENTATION_PLAN.md with findings
2. Update AGENTS.md if you learn new operational information
3. Commit changes with clear message: "Ralph: Investigation phase - [summary]"

IMPORTANT: Do NOT implement fixes in plan mode. Only investigate, document, and plan.
