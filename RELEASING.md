# HOWTO Release

0. Update the version and CHANGELOG
1. Commit and push
2. Create github release
3. Publish to Bintray
```
gradle clean bintrayUpload
```

Sync with Central every ~2h
