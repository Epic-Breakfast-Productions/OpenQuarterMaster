# Issue #1033: Lombok @SuperBuilder Intermittent Compilation Failure

## Problem Statement

Compilation of `oqm-core-api` intermittently fails with:
```
error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
```

The failure is **non-deterministic** - the same code may compile successfully on one run and fail on the next.

## Affected Module

- **Module**: `software/core/oqm-core-api`
- **Type**: Standalone Gradle project with its own `gradlew`
- **Technology**: Java 21, Quarkus 3.21.4, Lombok plugin 9.1.0

## Reported Class Hierarchy

The error reportedly occurs in the checkin transaction class hierarchy:

```
ItemStoredTransaction (abstract, @SuperBuilder, NO generics)
         ↓
CheckinTransaction<T extends CheckInDetails> (abstract, @SuperBuilder, HAS GENERIC)
         ↓
CheckinFullTransaction extends CheckinTransaction<ReturnFullCheckinDetails> (@SuperBuilder)
```

Location: `software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/`

## Hypothesis (To Be Verified)

1. **Race condition theory**: When Gradle compiles files in parallel or uses incremental compilation, annotation processors may run in non-deterministic order. If child classes are processed before parent class builders are fully generated, Lombok miscounts type arguments.

2. **lombok.config theory**: The setting `lombok.builder.className = Builder` in lombok.config may conflict with @SuperBuilder's requirement for class-prefixed builder names.

## What Must Be Verified (No Assumptions)

1. **Actual file contents**: Read and verify the actual class hierarchy exists as described
2. **Actual error reproduction**: Reproduce the actual error in the actual build environment
3. **Build environment**: Understand how builds actually run (CI uses GitHub Actions with JDK 21)
4. **Root cause confirmation**: Confirm the actual root cause through reproduction, not assumption

## Previous Invalid Fix

A PR standardized Lombok plugin versions across other modules (open-qm-core, open-qm-moduleDriver, etc.). This was invalid because:
- `oqm-core-api` is a standalone Gradle project
- It does not depend on those other modules
- It already had Lombok plugin 9.1.0

## Success Criteria

1. The actual error must be reproduced first
2. The fix must address the verified root cause
3. Validation must be meaningful:
   - Tests must actually run (not silently fail)
   - Build must actually compile (not skip compilation)
   - 10 consecutive clean builds must pass
4. CI pipeline must pass

## References

- Lombok #2647: lombok.builder.className=Builder breaks SuperBuilder
- Lombok #3911: SuperBuilder generics intermittent failures
- Lombok #3592: SuperBuilder wrong number of type arguments with annotations on generic type
