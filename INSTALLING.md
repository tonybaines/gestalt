# Installation
To install and use *Gestalt*, just point your dependency-management system of choice at the latest release (hosted in Maven Central)

#### [Gradle](http://gradle.org)
```groovy
'com.github.tonybaines:gestalt:1.0.0'
```


#### [Maven](http://maven.apache.org/)
```xml
<dependency>
    <groupId>com.github.tonybaines</groupId>
    <artifactId>gestalt</artifactId>
    <version>1.0.0</version>
</dependency>
```

#### [Ivy](https://ant.apache.org/ivy/)
```xml
<dependency org="com.github.tonybaines" name="gestalt" rev="1.0.0"/>
```

#### Manual installation
The latest gestalt release JAR is available in [Maven Central](http://central.maven.org/maven2/com/github/tonybaines/gestalt/).
Download and install it and the ```compile``` dependencies the same way you do for any other JAR

But really you should take a look at [Gradle](http://www.gradle.org/docs/current/userguide/tutorial_java_projects.html#N103D7)

## SNAPSHOT releases
Ant **SNAPSHOT** release will go into `https://oss.sonatype.org/content/repositories/snapshots/` - if you want to try a SNAPSHOT, adjust your build tool accordingly.
