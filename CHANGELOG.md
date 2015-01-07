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
