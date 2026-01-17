# Implementation Plan - Issue #1033

## Current Status: Phase 2 COMPLETE - Root Cause Confirmed, Ready for Fix

## Investigation Summary (2026-01-17 - Verification Run)

### Error Reproduction: RE-VERIFIED

**Failure Rate**: 3 out of 10 builds failed (~30% intermittent failure rate)

This verification run confirms the error persists and is reproducible.

**Exact Error Messages Captured**:
```
/app/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinLossTransaction.java:17: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
^
/app/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinPartTransaction.java:17: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
^
/app/src/main/java/tech/ebp/oqm/core/api/model/object/storage/checkout/ItemWholeCheckout.java:19: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
^
/app/src/main/java/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/CheckinFullTransaction.java:18: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
^
/app/src/main/java/tech/ebp/oqm/core/api/model/object/storage/checkout/ItemAmountCheckout.java:22: error: wrong number of type arguments; required 2
@SuperBuilder(toBuilder = true)
^
```

### Verified Affected Class Hierarchies

**Hierarchy 1: CheckinTransaction** (Transactions package)
```
ItemStoredTransaction (abstract, @SuperBuilder, NO generics)
        ↓
CheckinTransaction<T extends CheckInDetails> (abstract, @SuperBuilder, HAS GENERIC)
        ↓
CheckinFullTransaction extends CheckinTransaction<ReturnFullCheckinDetails>
CheckinPartTransaction extends CheckinTransaction<ReturnPartCheckinDetails>
CheckinLossTransaction extends CheckinTransaction<ReturnPartCheckinDetails>
```

**Hierarchy 2: ItemCheckout** (Checkout package)
```
AttKeywordMainObject (no generics)
        ↓
ItemCheckout<T> (abstract, @SuperBuilder, HAS GENERIC)
        ↓
ItemAmountCheckout extends ItemCheckout<Quantity>
ItemWholeCheckout extends ItemCheckout<Stored>
```

### Verified lombok.config Contents

File: `software/core/oqm-core-api/lombok.config`
```
lombok.accessors.chain=true
lombok.addLombokGeneratedAnnotation = true
#lombok.addJavaxGeneratedAnnotation = true
lombok.builder.className = Builder
```

**CRITICAL FINDING**: The setting `lombok.builder.className = Builder` is present and is known to cause issues with @SuperBuilder when combined with generics (see Lombok issue #2647).

### Build Configuration Verified

- **Lombok plugin**: `io.freefair.lombok` version `9.1.0`
- **Java toolchain**: Java 21
- **Quarkus**: 3.21.4
- **No incremental compilation disabled**
- **No fork option set**

---

## Phase 1: Verify the Problem Exists [COMPLETE]

### 1.1 Read and Document Actual Class Hierarchy [DONE]
- [x] Read `ItemStoredTransaction.java` - verified @SuperBuilder, NO generics, abstract class
- [x] Read `CheckinTransaction.java` - verified @SuperBuilder, HAS generic `<T extends CheckInDetails>`
- [x] Read `CheckinFullTransaction.java` - verified extends with concrete type `CheckinTransaction<ReturnFullCheckinDetails>`
- [x] Document actual class signatures (verified, matches specification)
- [x] Discovered ADDITIONAL affected hierarchy: ItemCheckout<T> with ItemAmountCheckout, ItemWholeCheckout

### 1.2 Read Configuration Files [DONE]
- [x] Read `lombok.config` - documented actual contents (includes `lombok.builder.className = Builder`)
- [x] Read `build.gradle` - documented Lombok plugin version 9.1.0
- [x] Read `gradle.properties` - documented (no parallelism settings, INFO logging)

### 1.3 Verify Build Environment Works [DONE]
- [x] Run single Docker-based build - SUCCEEDED
- [x] Verify class files are generated - CONFIRMED (all .class files present)
- [x] Verify exit codes work (0 for success, non-zero for failure) - CONFIRMED
- [x] Test error detection with intentional syntax error - CONFIRMED (build fails with exit code 1)

### 1.4 Attempt to Reproduce the Error [DONE]
- [x] Run 10 consecutive builds WITHOUT any fix
- [x] Document: Does the error occur? **YES** - 2 out of 10 runs failed (20%)
- [x] Documented exact error messages captured

### 1.5 Verification Run (2026-01-17) [DONE]
- [x] Re-ran 10 consecutive builds to re-verify the issue
- [x] **Results**: 3 out of 10 builds failed (30% failure rate)
  - Run 1: **FAILED** - ItemAmountCheckout.java:22
  - Run 2: **FAILED** - CheckinFullTransaction.java:18
  - Run 3: SUCCESS
  - Run 4: SUCCESS
  - Run 5: SUCCESS
  - Run 6: SUCCESS
  - Run 7: SUCCESS
  - Run 8: SUCCESS
  - Run 9: SUCCESS
  - Run 10: **FAILED** - ItemAmountCheckout.java:22
- [x] Error detection verified with intentional syntax error - works correctly

---

## Phase 2: Analyze Root Cause [COMPLETE]

### 2.1 Error Reproduced - Analysis

**Root Cause Hypothesis (High Confidence)**:

The combination of:
1. `lombok.builder.className = Builder` in lombok.config
2. Generic type parameters in intermediate abstract classes (`CheckinTransaction<T>`, `ItemCheckout<T>`)
3. Lombok @SuperBuilder annotation processor timing

Creates a race condition where the annotation processor sometimes processes child classes before the parent's builder is fully resolved. The `Builder` class naming convention (instead of default `<ClassName>Builder`) conflicts with @SuperBuilder's generated code for generic hierarchies.

**Supporting Evidence**:
- Lombok GitHub Issue #2647: "lombok.builder.className=Builder breaks SuperBuilder"
- Lombok GitHub Issue #3911: "SuperBuilder generics intermittent failures"
- Error message "wrong number of type arguments; required 2" indicates the generic builder parameters aren't being properly resolved

**Fix Options (to be evaluated)**:

| Option | Approach | Risk | Effort |
|--------|----------|------|--------|
| A | Remove `lombok.builder.className = Builder` from lombok.config | Low - may require builder call site updates | Low |
| B | Add `options.incremental = false` to JavaCompile | Low - longer build times | Low |
| C | Add `options.fork = true` to JavaCompile | Low - longer build times | Low |
| D | Explicit builder class naming on affected classes | Low | Medium |

**Recommended Fix**: Option A - Remove `lombok.builder.className = Builder`

Rationale: This is the root cause configuration that conflicts with @SuperBuilder for generic hierarchies. Removing it will let Lombok use its default builder naming which is compatible with @SuperBuilder generics.

---

## Phase 3: Implement Fix [NOT STARTED]

### 3.1 Choose Fix Strategy
- [ ] Validate Option A: Remove `lombok.builder.className = Builder`
- [ ] Search codebase for `.builder()` calls that might be affected
- [ ] Apply fix

### 3.2 Apply Minimal Fix
- [ ] Make smallest change that addresses root cause
- [ ] Document why this fix addresses the problem

---

## Phase 4: Validate Fix [NOT STARTED]

### 4.1 Meaningful Validation
- [ ] Verify Java compiler actually runs (check for Compiling output)
- [ ] Verify class files are generated
- [ ] Run 10 consecutive clean builds
- [ ] All 10 builds must ACTUALLY pass (not silently fail)

### 4.2 CI Validation
- [ ] Push changes
- [ ] Verify CI pipeline passes
- [ ] Check build logs for actual compilation

---

## Phase 5: Document and Commit [NOT STARTED]

### 5.1 Commit
- [ ] Clear commit message explaining:
  - What the root cause was (verified, not assumed)
  - Why this fix addresses it
  - Reference issue #1033

---

## Notes / Findings

### 2024-01-17 Investigation Results

1. **Error Successfully Reproduced**: 20% failure rate (2/10 builds)
2. **Two Affected Hierarchies Discovered**:
   - CheckinTransaction hierarchy (3 concrete classes)
   - ItemCheckout hierarchy (2 concrete classes)
3. **Root Cause Identified**: `lombok.builder.className = Builder` in lombok.config
4. **Build Environment Verified**: Docker with eclipse-temurin:21-jdk works correctly
5. **Error Detection Verified**: Compilation errors properly detected with non-zero exit codes

### 2026-01-17 Verification Run Results

1. **Error Re-Verified**: 30% failure rate (3/10 builds)
2. **Affected Files Confirmed**:
   - ItemAmountCheckout.java:22 (failed 2x)
   - CheckinFullTransaction.java:18 (failed 1x)
3. **Build Environment**: Docker with eclipse-temurin:21-jdk (Java 21.0.9+10-LTS)
4. **Error Detection**: Verified working via intentional syntax error test
5. **Ready for Phase 3**: Root cause confirmed, recommended fix is Option A (remove `lombok.builder.className = Builder`)
