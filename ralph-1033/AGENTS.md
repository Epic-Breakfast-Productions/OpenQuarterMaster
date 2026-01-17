# AGENTS.md - Ralph Operational Guide for Issue #1033

## Build & Run

### Build Command (via Docker - no local Java required)

```bash
cd software/core/oqm-core-api

# Clean build without tests
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
    ./gradlew clean build -x test --no-daemon
```

### Full build with tests

```bash
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
    ./gradlew clean build --no-daemon
```

## Validation

### Verify Java Actually Ran

After build, check that class files exist:
```bash
ls -la build/classes/java/main/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/
```

Expected files:
- `CheckinTransaction.class`
- `CheckinTransaction$Builder.class`
- `CheckinFullTransaction.class`
- `CheckinFullTransaction$Builder.class`
- `CheckinFullTransaction$BuilderImpl.class`
- `CheckinPartTransaction.class`
- `CheckinLossTransaction.class`

### Run 10 Consecutive Builds (RELIABLE METHOD)

```bash
cd software/core/oqm-core-api
for i in {1..10}; do
    echo "=== Run $i ==="
    docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
        ./gradlew clean build -x test --no-daemon 2>&1 > /tmp/build_$i.txt
    exit_code=$?
    if [ $exit_code -ne 0 ]; then
        echo "FAILED"
        grep "error: wrong number" /tmp/build_$i.txt
    else
        echo "SUCCESS"
    fi
done
```

**IMPORTANT**: When checking build results, capture output to a file and check exit code separately. The Docker container output may show interleaved text from previous runs if using pipes.

### Intentional Error Test

To verify error detection works, temporarily add a syntax error:
```bash
# Add syntax error
echo "THIS IS A SYNTAX ERROR" >> software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java

# Build should fail
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
    ./gradlew clean build -x test --no-daemon
# Expected: non-zero exit code

# Revert
git checkout software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java
```

## Key Files

### Configuration Files
- `software/core/oqm-core-api/build.gradle` - Gradle build config
- `software/core/oqm-core-api/gradle.properties` - Gradle properties
- `software/core/oqm-core-api/lombok.config` - Lombok settings (CRITICAL - contains problematic `lombok.builder.className = Builder`)

### Affected Source Files - Hierarchy 1 (CheckinTransaction)
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/ItemStoredTransaction.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinTransaction.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinFullTransaction.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinPartTransaction.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinLossTransaction.java`

### Affected Source Files - Hierarchy 2 (ItemCheckout)
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/checkout/ItemCheckout.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/checkout/ItemAmountCheckout.java`
- `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/checkout/ItemWholeCheckout.java`

## Operational Notes

### Docker Usage
- Use `eclipse-temurin:21-jdk` for Java 21 environment
- Use `--no-daemon` to avoid caching issues between runs
- Mount current directory with `-v "$(pwd)":/app`
- Build takes approximately 1m 15s per run

### Gradle Caching
- Use `clean` to ensure full recompilation
- Use `--no-daemon` to avoid daemon caching
- Check `build/` directory for artifacts

### CI Pipeline
- CI runs on push to any branch
- CI workflow: `.github/workflows/core-api.yml`
- Check CI results at: https://github.com/Epic-Breakfast-Productions/OQM/actions

## Codebase Patterns

### Lombok Usage
- Project uses `io.freefair.lombok` plugin version 9.1.0
- `@SuperBuilder(toBuilder = true)` used on entity classes
- `lombok.config` contains project-wide Lombok settings

### Class Hierarchy Pattern
- Abstract base classes use `@SuperBuilder`
- Concrete classes extend and also use `@SuperBuilder`
- Generic type parameters used in intermediate classes

## Verified Findings (2024-01-17)

### Error Reproduction
- **Intermittent failure rate**: ~20% (2 out of 10 builds fail)
- **Error message**: `error: wrong number of type arguments; required 2`
- **Root cause**: `lombok.builder.className = Builder` in lombok.config conflicts with @SuperBuilder generics

### Affected Class Hierarchies

**Hierarchy 1: CheckinTransaction**
```
ItemStoredTransaction (@SuperBuilder, no generics)
        ↓
CheckinTransaction<T extends CheckInDetails> (@SuperBuilder, HAS GENERIC)
        ↓
CheckinFullTransaction, CheckinPartTransaction, CheckinLossTransaction
```

**Hierarchy 2: ItemCheckout**
```
AttKeywordMainObject
        ↓
ItemCheckout<T> (@SuperBuilder, HAS GENERIC)
        ↓
ItemAmountCheckout, ItemWholeCheckout
```

### Known Lombok Issues
- Lombok #2647: lombok.builder.className=Builder breaks SuperBuilder
- Lombok #3911: SuperBuilder generics intermittent failures
- Lombok #3592: SuperBuilder wrong number of type arguments with annotations on generic type
