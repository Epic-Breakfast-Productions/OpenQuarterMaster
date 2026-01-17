# Issue #1033: Sporadic Lombok SuperBuilder Compilation Failure

## Problem Summary

The Core API project has a sporadic/flaky compilation failure during Lombok annotation processing. The `@SuperBuilder` annotation intermittently fails to generate builder code correctly, causing compilation errors. This appears to be a race condition in the annotation processor.

## Error Details

```
OpenQuarterMaster/software/core/oqm-core-api/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinFullTransaction.java:18: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
```

## Affected Locations

Primary locations where errors manifest:
- `model/object/storage/items/transactions/transactions/checkout/`
- `model/object/storage/items/transactions/transactions/checkin/`

## Reproduction

```bash
cd software/core/oqm-core-api
./gradlew clean build -x test
```

The error occurs more often than not but is intermittent. Multiple consecutive runs may produce different results.

## Investigation Tasks

### Task 1: Analyze Class Hierarchy
- Examine `CheckinFullTransaction.java` and related classes in checkout/checkin directories
- Map out the full inheritance hierarchy
- `@SuperBuilder` issues often stem from complex inheritance chains with generics
- Document the type parameter flow through the hierarchy

### Task 2: Review Lombok Configuration
- Check for `lombok.config` files in the project
- Verify Gradle Lombok plugin version in `build.gradle` or `build.gradle.kts`
- Review annotation processor configuration
- Check for any custom Lombok configuration that might affect code generation

### Task 3: Examine All @SuperBuilder Usages
- Find all classes using `@SuperBuilder` annotation
- Identify patterns, especially those with generics
- The error "wrong number of type arguments; required 2" suggests a generics mismatch
- Look for inconsistencies in how type parameters are declared vs used

### Task 4: Check for Known Lombok Issues
- Research known Lombok bugs related to `@SuperBuilder` with generics
- Check Lombok GitHub issues for similar problems
- Look for race conditions during annotation processing
- Review Lombok changelog for relevant fixes in newer versions

### Task 5: Analyze Build Configuration
- Examine annotation processor ordering
- Check parallel compilation settings (`org.gradle.parallel`)
- Review incremental compilation configs
- Look for any build caching that could cause stale generated code
- Check Java compiler fork settings

### Task 6: Identify Potential Fixes
Based on findings, document solutions with pros/cons:
- Lombok version upgrade
- Annotation processor ordering fixes
- Build configuration changes (disabling parallel annotation processing)
- Code refactoring to simplify the inheritance hierarchy
- Adding explicit generic type parameters
- Removing `@SuperBuilder` in favor of manual builder implementation

## Success Criteria

The fix is complete when **10 consecutive runs** of `./gradlew clean build -x test` succeed without hitting this error.

Validation script:
```bash
cd software/core/oqm-core-api
for i in {1..10}; do
    echo "=== Run $i ==="
    ./gradlew clean build -x test || { echo "FAILED on run $i"; exit 1; }
done
echo "SUCCESS: All 10 runs passed"
```

## Deliverables

1. **Root Cause Analysis**: Detailed explanation of why the error occurs
2. **Class Hierarchy Documentation**: Diagram or description of affected class hierarchies
3. **Configuration Audit**: Lombok and build configuration findings
4. **Recommended Fixes**: Prioritized list of solutions with pros/cons
5. **Related Issues**: Any other problems discovered during investigation
6. **Implementation**: If a fix is identified, implement and verify with 10 consecutive builds

## Related Information

- This issue was previously encountered during Issue #868 (Support Optional JWT Fields) work
- The bug appears to be related to Lombok issue #2359
- Affects classes using `@SuperBuilder` with generic hierarchies
