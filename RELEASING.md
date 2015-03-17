# HOWTO Release

0. Update the version and CHANGELOG
1. Commit and push
2. Create github release
3. Publish to Sonatype
```
gradle upload
```
4. Login to https://oss.sonatype.org/
5. Select 'Staging Repositories'
6. Find tonybaines, select it
7. Choose 'Close' - wait for successful completion
8. Choose 'Release' - should get an email

Sync with Central every ~2h
