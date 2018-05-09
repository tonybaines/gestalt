# HOWTO Release

1. Update the version and CHANGELOG
```
./update-version-number.sh
```
2. Push
3. Create github release
4. Publish to Bintray
```
./gradlew clean bintrayUpload
```
Sync with Central every ~2h

5. Update the version in build.gradle to the next SNAPSHOT