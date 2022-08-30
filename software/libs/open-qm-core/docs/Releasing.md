# Releasing

1. Setup:
   1. Have pgp public key 
      1. https://central.sonatype.org/publish/requirements/gpg/
   2. create `gradle.properties`, fill out
2. Check for outdated dependencies
3. Run _all_ tests:
    1. `./gradlew clean test`
4. Bump version appropriately
5. Publish