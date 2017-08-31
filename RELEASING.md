# HOWTO Release

0. Update the version and CHANGELOG
```
./update-version-number.sh
```
1. Push
2. Create github release
3. Publish to Bintray
```
./gradlew clean bintrayUpload
```
4. Update to next SNAPSHOT version

Sync with Central every ~2h

5. Update the version in build.gradle to the next SNAPSHOT