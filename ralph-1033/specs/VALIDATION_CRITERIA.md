# Validation Criteria

## Meaningful Test Execution

A test is only meaningful if:

### 1. Java Actually Runs

**MUST verify**: The Java compiler actually executed
- Check for "Compiling" output in Gradle logs
- Check for class files being generated
- Check for non-zero exit code if compilation fails

**INVALID**: Tests that "pass" because Java wasn't installed
- Exit code 0 with "Unable to locate a Java Runtime" is NOT a pass

### 2. Affected Classes Are Compiled

**MUST verify**: The specific classes are compiled:
- `ItemStoredTransaction.java`
- `CheckinTransaction.java`
- `CheckinFullTransaction.java`
- `CheckinPartTransaction.java`
- `CheckinLossTransaction.java`

**How to verify**:
```bash
# Check class files exist after build
ls -la build/classes/java/main/tech/ebp/oqm/core/api/model/object/storage/items/transactions/
```

### 3. Error Detection Works

**MUST verify**: Compilation errors are actually detected and reported
- Intentionally introduce a syntax error and verify build fails
- Verify the specific "wrong number of type arguments" error can be detected

### 4. Intermittent Failure Is Reproducible

Before claiming a fix:
- Run at least 10 consecutive builds WITHOUT the fix
- Document whether the failure occurs (and how often)
- If failure doesn't occur, we may not have the right reproduction conditions

## Test Execution Method

### Option A: Docker (Recommended)

```bash
cd software/core/oqm-core-api

# Single build test
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
    ./gradlew clean build -x test --no-daemon

# Verify compilation happened
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk \
    ls -la build/classes/java/main/tech/ebp/oqm/core/api/model/object/storage/items/transactions/transactions/checkin/
```

### Option B: GitHub Actions

Push changes to a branch and observe CI results at:
`https://github.com/Epic-Breakfast-Productions/OQM/actions`

### Option C: Local Java (if installed)

```bash
cd software/core/oqm-core-api
java -version  # Verify Java 21 is available
./gradlew clean build -x test
```

## Success Criteria Checklist

- [ ] Java compiler actually runs
- [ ] Affected class files are generated
- [ ] Compilation errors are detectable (test with intentional error)
- [ ] Intermittent failure reproduction attempted (10+ builds without fix)
- [ ] Fix applied
- [ ] 10 consecutive clean builds pass WITH the fix
- [ ] CI pipeline passes
- [ ] Fix is minimal and targeted
