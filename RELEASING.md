# HOWTO Release

0. Update the version
1. Publish to Sonatype
```
gradle upload
```
2. Login to https://oss.sonatype.org/
3. Select 'Staging Repositories'
4. Find tonybaines, select it
5. Choose 'Close' - wait for successful completion
6. Choose 'Release' - should get an email

Sync with Central every ~2h