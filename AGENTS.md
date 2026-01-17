# OQM Agents Guide - Lombok Investigation

## Project Overview

Open QuarterMaster (OQM) - Modular inventory management system built with Java 21 / Quarkus.

**Current Focus**: Investigating sporadic Lombok @SuperBuilder compilation failure (Issue #1033)

## Build Commands

### Core API (Primary Investigation Target)
```bash
cd software/core/oqm-core-api

# Clean build without tests (use this for validation)
./gradlew clean build -x test

# Full build with tests
./gradlew build

# Build with verbose output (for debugging)
./gradlew clean build -x test --info
```

### 10-Run Validation Script
```bash
cd software/core/oqm-core-api
for i in {1..10}; do
    echo "=== Run $i ==="
    ./gradlew clean build -x test || { echo "FAILED on run $i"; exit 1; }
done
echo "SUCCESS: All 10 runs passed"
```

## Key Directories

| Component | Path |
|-----------|------|
| Core API | `software/core/oqm-core-api/` |
| Affected Classes | `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/` |
| Build Config | `software/core/oqm-core-api/build.gradle` |

## Investigation Targets

### Files to Examine
- `CheckinFullTransaction.java` - Error manifests here
- `CheckoutFullTransaction.java` - Similar pattern
- Parent classes in the transaction hierarchy
- `lombok.config` - Lombok configuration
- `build.gradle` - Build and annotation processor config

### Search Patterns
```bash
# Find all @SuperBuilder usages
grep -r "@SuperBuilder" software/core/oqm-core-api/

# Find lombok config
find . -name "lombok.config"

# Check Lombok version in build files
grep -r "lombok" software/core/oqm-core-api/build.gradle
```

## Technology Stack

- **Java**: 21
- **Framework**: Quarkus 3.21.4
- **Build**: Gradle (wrapper)
- **Lombok**: Version TBD (investigate build.gradle)

## Operational Notes

### Known Issues
- @SuperBuilder with complex generic hierarchies causes intermittent compilation failures
- Error: "wrong number of type arguments; required 2"
- Appears to be a race condition in annotation processing
- Related to Lombok GitHub issue #2359

### Previous Workaround Attempts (from #868)
1. Downgrading Lombok Freefair plugin to 8.6, 8.12.2 - no effect
2. Single-threaded compilation - still fails
3. Inlining changes instead of helper methods - still fails
4. Moving changes to non-@SuperBuilder classes - InteractingEntity/User changes still trigger bug
