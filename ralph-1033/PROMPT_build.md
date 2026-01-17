You are fixing Lombok @SuperBuilder issue #1033 in the OQM project.

## Critical Requirements

1. **Make NO assumptions** - verify everything by reading actual files
2. **Tests must be meaningful** - if Java doesn't run, that's NOT a passing test
3. **Use Docker for builds** - local Java may not be installed
4. **Fix must address verified root cause** - not assumed cause

## Your Task: Implementation

### Step 0: Context

0a. Read `ralph-1033/specs/*` to understand the issue specification.
0b. Read `ralph-1033/IMPLEMENTATION_PLAN.md` to see current progress and findings.
0c. Read `ralph-1033/AGENTS.md` for operational commands.

### Step 1: Verify Investigation Complete

Check IMPLEMENTATION_PLAN.md for completed investigation:
- [ ] Class hierarchy verified
- [ ] Configuration files documented
- [ ] Build environment tested
- [ ] Error reproduction attempted

If investigation is incomplete, complete it first before implementing fixes.

### Step 2: Implement Fix (If Investigation Complete)

Based on the verified root cause in IMPLEMENTATION_PLAN.md:

1. Choose the minimal fix that addresses the actual root cause
2. Make the changes to the appropriate files
3. Document what you changed and why

### Step 3: Validate Fix (Meaningful Validation)

Run validation using Docker:

```bash
cd software/core/oqm-core-api

# Verify build works
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test --no-daemon

# Verify class files exist (proof compilation happened)
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ls -la build/classes/java/main/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/

# Run 10 consecutive builds
for i in {1..10}; do
    echo "=== Run $i ==="
    docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test --no-daemon
    if [ $? -ne 0 ]; then
        echo "FAILED on run $i"
        exit 1
    fi
    echo "Run $i: SUCCESS"
done
echo "All 10 runs passed"
```

### Step 4: Run Full Tests (If Build Passes)

```bash
cd software/core/oqm-core-api
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean test --no-daemon
```

### Step 5: Update Documentation

1. Update IMPLEMENTATION_PLAN.md:
   - Mark fix as implemented
   - Document validation results
   - Note any issues encountered

2. Update AGENTS.md if you learned new operational information

### Step 6: Commit

If validation passes:

```bash
git add -A
git commit -m "$(cat <<'EOF'
Fix #1033: [Brief description of fix]

Root cause: [What was actually verified to be the cause]

Changes:
- [List changes made]

Validation:
- [How validation was performed]
- [Results of 10 consecutive builds]

Co-Authored-By: Claude Opus 4.5 <noreply@anthropic.com>
EOF
)"
git push
```

### Step 7: Create Git Tag (If All Tests Pass)

```bash
# Check existing tags
git tag -l | sort -V | tail -5

# Create new patch version tag
git tag -a v[X.Y.Z] -m "Fix #1033: Lombok @SuperBuilder compilation fix"
git push origin v[X.Y.Z]
```

## Validation Checklist (All Must Be True)

- [ ] Java compiler actually ran (not "Unable to locate Java Runtime")
- [ ] Class files exist in build/classes/...
- [ ] 10 consecutive Docker-based builds passed
- [ ] Full test suite passes
- [ ] Fix addresses verified root cause (not assumed)
- [ ] Changes are minimal and targeted

## Important Notes

- If validation shows Java didn't actually run, the test is INVALID
- If class files don't exist, compilation didn't happen
- If you can't reproduce the original error, document that finding
- Only commit if validation is meaningful and passes
