To install and use *Gestalt*, first download a release ZIP from https://github.com/tonybaines/gestalt/releases

Unpacking the ZIP you will find two files

    com.github.tonybaines.gestalt-<version>.zip
       |
       |- pom.xml
       |- com.github.tonybaines.gestalt-<version>.jar

The dependencies in ```pom.xml``` need to be available at runtime [1], either

* Create the appropriate runtime dependencies in your ```ivy.xml```/```build.gradle```
* Copy the runtime dependencies into your ```pom.xml```
* Download them separately (vanilla Ant)

You'll need to copy/declare/locally-publish the ```com.github.tonybaines.gestalt-<version>.jar``` as appropriate for your
build system

[1] Probably.  If you're not using JSR-303 validation then hibernate-validator, javax.el-api and javax.el
    may not be needed
