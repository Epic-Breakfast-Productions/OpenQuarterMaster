# Build Environment Specification

## CI Environment (GitHub Actions)

The actual build runs in GitHub Actions:

- **Runner**: `ubuntu-latest`
- **JDK**: 21 (Adopt distribution)
- **Build tool**: Gradle with `gradle-build-action@v2`
- **Build command**: `gradle build -x test` (for build job)

### CI Workflow Files

- `.github/workflows/core-api.yml` - Main workflow
- `.github/workflows/wf-gradleBuild.yaml` - Build step
- `.github/workflows/wf-gradleUnitTest.yaml` - Unit tests
- `.github/workflows/wf-gradleQuarkusIntTest.yaml` - Integration tests

## Local Development

For local testing, you need:
1. JDK 21 installed
2. The Gradle wrapper at `software/core/oqm-core-api/gradlew`

## Docker-Based Local Testing

Since Java may not be installed locally, use Docker to simulate CI:

```bash
cd software/core/oqm-core-api
docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test
```

Or for repeated testing:
```bash
for i in {1..10}; do
    echo "=== Run $i ==="
    docker run --rm -v "$(pwd)":/app -w /app eclipse-temurin:21-jdk ./gradlew clean build -x test
    if [ $? -ne 0 ]; then
        echo "FAILED on run $i"
        exit 1
    fi
done
echo "SUCCESS: All 10 runs passed"
```

## Validation Requirements

A valid test must:
1. Actually invoke the Java compiler (not silently fail)
2. Actually compile the affected classes
3. Report compilation errors if they occur
4. Return non-zero exit code on failure
5. Be repeatable to catch intermittent failures

## Files to Monitor

- `software/core/oqm-core-api/build.gradle` - Build configuration
- `software/core/oqm-core-api/gradle.properties` - Gradle settings
- `software/core/oqm-core-api/lombok.config` - Lombok configuration
- `software/core/oqm-core-api/src/main/java/.../transactions/` - Affected classes
