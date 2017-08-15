# [2.0.4](http://central.maven.org/maven2/com/github/tonybaines/gestalt/2.0.4)
* Issue #22 Provide a config source that accepts an existing config instance
* Introduced the `@NoCache` anotation to disable caching in a targeted way

# [2.0.3](http://central.maven.org/maven2/com/github/tonybaines/gestalt/2.0.3)
* Pegging dependency versions for slf4j-api and Guava

# [2.0.2](http://central.maven.org/maven2/com/github/tonybaines/gestalt/2.0.2)
* Fix for XML Serialisation issue

# [2.0.1](http://central.maven.org/maven2/com/github/tonybaines/gestalt/2.0.1) (2016-04-16)
* `@Optional` annotation for properties which need not be defined
* Issue #5: Custom validation (via default methods in config interfaces)

# [2.0.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/2.0.0) (2016-03-29)
* *BREAKING CHANGE*: Recompiled with Java 8
* Verified to work with default methods defined in interfaces
* Workaround for JaCoCo '$jacoco' properties appearing in Java 8

# [1.0.5](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.5) (2015-10-05)
* Support for List types defaulting to an empty List

# [1.0.4](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.5) (2015-10-01)
* Issue #21: Cosmetic fix when serialising list types to XML with property-name transformers

# [1.0.3](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.3) (2015-10-01)
* Issue #20: Missing properties for list types in config sources treated as empty lists

# [1.0.2](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.2) (2015-07-22)
* Upgrade to depend on [Groovy 2.4.4](http://groovy-lang.org/security.html)
* Support for custom `ConfigSource` implementations to supply property values

# [1.0.1](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.1) (2015-07-12)
* Relax the dependency constraints on Guava and SLF4J, remove unused dependencies

# [1.0.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/1.0.0) (2015-07-12)
* *N.B.* Potentially breaking changes from updated dependencies
* Switch to Apache bval for JSR-303 validation to reduce the risk of dependency clashes (vs. Hibernate)
* Reduce the verbosity of error logging and exceptions
* Switch to Groovy 2.4.3

# [0.8.9](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.9) (2015-03-26)
* Fix for the scope of all dependencies being set to 'runtime' in the generated POM

# [0.8.8](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.8) (2015-03-25)
* Issue #19: Validation of custom types gives a MissingPropertyException
* Publishing via bintray
* Upgrade to Spock 1.0

# [0.8.7](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.7) (2015-03-17)
* Issue #18 - default values for concrete types caused a ConfigurationException
* Logging improvements for error handling and validation messages
* gradle-nexus-plugin updated to 2.2
* Gradle wrapper updated to 2.2


# [0.8.6](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.6) (2015-01-07)
* When falling back to default, and when no value is found for a property (including default), log the full path rather than just the current property getter
* Issue #17: False boolean value is treated as not found

# [0.8.5](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.5) (2015-01-06)
* Issue #16: Java client gets exception when calling toString()
* Compiled for Java 7 again
* The published POM no longer depends on 'org.slf4j:slf4j-simple:1.6.1', but on the API JAR.  Clients can/should provide their own implementation

# [0.8.4](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.4) (2014-12-31)
**Compiled for Java 8**

* Issue #13: Support for Comments
* Issue #15: Support for Custom Types
* Issue #4: Obfuscation Support
* Issue #3: Support for mapping camel case to dashes
* Issue #12: NPE when accessing unassigned primitive values (fixed in README.md)
* Issue #10: NPE when serialising a config instance with null/missing values to a Properties instance
* Issue #9: Converting to a Properties instance more than two levels deep fails

# [0.8.3](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.3) (2014-06-29)
* Issue #8: (null values being serialised to a Properties instance)

# [0.8.2](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.2) (2014-06-26)
* Issue #2 : Maven dependencies not working
* Issue : XML parsing for undefined values below the topmost level (e.g. sub-interfaces) returned an empty string instead of null.

# [0.8.1](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.1) (2014-03-17)
* Minor bugfix

# [0.8.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.8.0) (2014-03-16)
* Whole-instance validation checking/reporting

# [0.7.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.7.0) (2014-03-11)
* Serialise to Properties

# [0.6.1](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.6.1) (2014-03-09)
* Bug fixes

# [0.6.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.6.0) (2014-03-08)
* Define and reuse constants in config files

# [0.5.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.5.0) (2014-03-07)
* Read config values from XML attributes
* Allow a specific class to be supplied when loading a resource to simplify locating it
* Bug fixes / improved Java interoperability

# [0.4.0](http://central.maven.org/maven2/com/github/tonybaines/gestalt/0.4.0) (2014-03-04)
* First Maven Central release
