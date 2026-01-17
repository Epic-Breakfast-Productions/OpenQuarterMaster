# Lombok SuperBuilder Investigation Plan

*Last Updated: 2026-01-17*
*Status: Investigation Not Started*

## Overview

Investigating sporadic/flaky compilation failure in Core API caused by Lombok `@SuperBuilder` annotation processing race condition.

**Issue**: #1033
**Error**: `wrong number of type arguments; required 2` at `@SuperBuilder(toBuilder = true)`
**Affected**: Checkin/Checkout transaction classes

---

## Investigation Progress

### Phase 1: Class Hierarchy Analysis
- [ ] Map CheckinFullTransaction inheritance chain
- [ ] Map CheckoutFullTransaction inheritance chain
- [ ] Document all type parameters in hierarchy
- [ ] Identify problematic generic patterns

### Phase 2: @SuperBuilder Audit
- [ ] Find all @SuperBuilder usages in codebase
- [ ] Catalog generic type parameters for each
- [ ] Identify inconsistencies or patterns
- [ ] Note which classes use `toBuilder = true`

### Phase 3: Configuration Audit
- [ ] Find and review lombok.config files
- [ ] Document Lombok version and plugin
- [ ] Review annotation processor settings
- [ ] Check parallel/incremental build settings

### Phase 4: Root Cause Analysis
- [ ] Correlate findings with known Lombok issues
- [ ] Identify race condition triggers
- [ ] Understand why error is intermittent
- [ ] Document root cause

### Phase 5: Fix Recommendations
- [ ] List potential fixes with pros/cons
- [ ] Prioritize by risk and effort
- [ ] Document recommended solution

---

## Findings

### Class Hierarchy
*To be populated during investigation*

### Lombok Configuration
*To be populated during investigation*

### Build Configuration
*To be populated during investigation*

### Root Cause
*To be populated during investigation*

---

## Recommended Fixes

*To be populated after investigation*

| Priority | Fix | Pros | Cons | Risk |
|----------|-----|------|------|------|
| 1 | TBD | | | |
| 2 | TBD | | | |
| 3 | TBD | | | |

---

## Validation Log

Track each build attempt here:

| Attempt | Date | Fix Applied | Result | Notes |
|---------|------|-------------|--------|-------|
| | | | | |

---

## Related Issues

- #868 - Support Optional JWT Fields (DEFERRED due to this bug)
- Lombok GitHub #2359 - Known @SuperBuilder generics issue

---

## Notes

- Error is intermittent - may pass some builds and fail others
- Success criteria: 10 consecutive `./gradlew clean build -x test` passes
- Previous workaround attempts for #868 did not resolve the underlying issue
