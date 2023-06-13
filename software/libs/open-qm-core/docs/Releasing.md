# Releasing

1. Setup:
   1. Have pgp public key 
      1. https://central.sonatype.org/publish/requirements/gpg/
      2. Easiest to copy `~/.gnupg` from pre-setup
   2. create `gradle.properties`, fill out
2. Check for outdated dependencies
3. Run _all_ tests:
   1. `./gradlew clean test`
4. Bump version appropriately
   1. Tags:
      1. `DEV` - for development versions
      2. `SNAPSHOT` - for snapshots of current code
      3. `FINAL` - for finalized and tested versions
5. Publish