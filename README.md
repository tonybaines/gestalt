# *Gestalt* Configuration [![Build Status](https://travis-ci.org/tonybaines/gestalt.png)](https://travis-ci.org/tonybaines/gestalt)


## What's it for?
Configuration in Java is usually semi-weakly-typed e.g. the key-value store of a `.properties` file

```properties
thing.name='foo'
thing.size=7
thing.enabled=true
```

or a home-grown parse/extract from a more explicitly hierarchical scheme e.g. XML

```xml
<myconfig>
  <thing>
    <name>foo</name>
    <size>7</size>
    <enabled>true</enabled>
  </thing>
</myconfig>
```

Working with the results of reading in these files inevitably means a good deal of boilerplate for

* Checking for undefined-values/null checks
* Converting to an explict type (String|Integer|...)
* Handling defaults
* Validating the values
* Reading and combining from multiple sources (e.g. per-user or per-host overrides)

In a strongly, statically typed language such as Java we like to use objects to represent state e.g.

```java
public interface ThingConfig {
  String getName();
  Integer getSize();
  Boolean getEnabled();
}
```

If configuration options are represented by an instance of `ThingConfig` then they can be safely passed as a single
parameter and created as anonymous instances for test-injection.

## Getting Started

Using *Gestalt* can be as simple as

```java
ThingConfig config = Configurations
                         .definedBy(ThingConfig.class)
                         .fromPropertiesResource("thing.properties")
                         .done();

assertEquals("foo", config.getName());
assertEquals(7, config.getSize());
assertEquals(true, config.getEnabled());
```

Your definition of the interface drives what properties are expected to be defined in the underlying source files.

Follow the instructions in [INSTALLING](INSTALLING.md) to prepare your runtime environment/IDE.

## Defining the Config interface

The interface you use to describe your configuration needs to follow some rules, the [examples used in the tests](src/test/resources)
are used to exercise all the features, and are also commented to explain non-obvious behaviour

### Method Names
You need to follow the Java Bean conventions for property access, basically ```getFoo()``` with the option of ```isFoo()``` for ```boolean``` properties

The name of the method and the content of the configuration resource need to match e.g.

```java
String getFoo();
```
Would correspond to a configuration

```xml
...
<foo>the value of foo</foo>
...
```

or

```properties
...foo=the value of foo
```

### Types
The following types (and their primitive counterparts where applicable) are supported as return types
* String
* Integer
* Boolean
* Double
* Long
* Any ```Enum```
* A ```List<T>``` where *T* **must** be declared for the reflection to work
* Another Config interface (see below)

### Hierarchy
If your configuration requires nested types, then the return another interface e.g.

```java
public interface TopLevelConfig {
...
  SecondLevelConfig getSecondLevel();
...
}

public interface SecondLevelConfig {
    String getFoo();
    Boolean isBar();
}
```

This can be repeated for as many levels as necessary

### Default Values
Out-of-the-box, an undefined property results in a runtime exception, but we can define defaults in the interface

```java
public interface ThingConfig {
  @Default.String("bar")
  String getName();

  @Default.Integer(42)
  Integer getSize();

  @Default.Boolean(false)
  Boolean getEnabled();
}
```

So a lookup to ```config.getSize()``` when the value isn't explicitly defined will return ```42```

### Constants
Sometimes it's useful to able to declare and reuse constant values multiple times in a configuration, perhaps
on a per-user or per-host basis.  The ```withConstants(...)``` & ```withConstantsFromResource(...)```
methods inject a ```Map``` or ```Properties``` instance containing simple key-value
definitions for constants e.g.

```properties
NAME = bar
LEVEL = 11
ENABLED = true
```

The configuration file references the constants using their keys, wrapped in ```%{...}``` e.g.
```xml
<simple>
    <name>%{NAME}</name>
    <level>11</level>
    <enabled>%{ENABLED}</enabled>
    <sub>
      <id>%{NAME}</id>
      <switched-on>%{ENABLED}</switched-on>
    </sub>
</simple>
```

... and brought together like this.

```java
// This could be created
SimpleConfig config = Configurations.definedBy(SimpleConfig.class)
                        .fromGroovyConfigResource("config.xml")
                        .withConstantsFromResource("constants.properties")
                        .done();
```



### Multiple sources

Multiple sources can be combined from XML, ```.properties``` and [GroovyConfig](http://groovy.codehaus.org/gapi/groovy/util/ConfigSlurper.html) (last definition wins)

```java
ThingConfig config = Configurations.definedBy(ThingConfig.class)
                        .fromPropertiesResource("common.properties")
                        .fromXmlResource("common.xml")
                        .fromGroovyConfigResource("common.grc")
                        .fromPropertiesResource(System.getProperty("user.name")+".properties")
                        .done();
```

#### Optional Sources

A source can be declared as optional (i.e. it may or may not exist at runtime)

```java
ThingConfig config = Configurations.definedBy(ThingConfig.class)
        .fromPropertiesResource("common.properties")
        .fromPropertiesResource(System.getProperty("user.name")+".properties", isOptional)
        .done();
```

The default is for a runtime exception to be thrown, this will also happen if there are no valid sources at all.

#### Locating Resources to load

If your config source is not in the root of the classpath it can be cumbersome to locate,

```java
Configurations
  .definedBy(SimpleConfig.class)
  .fromXmlResource("com/github/tonybaines/gestalt/config/simple-config.xml")
  .done();
```

There is an overloaded version of the 'from...' methods which accepts a Class instance, which will be used for relative lookups if provided.

e.g. if you have a Class in the package ```com.github.tonybaines.gestalt``` the path can be simplified as follows

```java
Configurations
  .definedBy(SimpleConfig.class)
  .fromXmlResource("config/simple-config.xml", this.getClass())
  .done();
```

### Validation

Validation can be defined for one or more properties using the [JSR-303 Bean Validation annotations](http://docs.oracle.com/javaee/6/api/javax/validation/constraints/package-summary.html)  e.g.

```java
public interface ThingConfig {

  @Size(min = 1, max = 10)
  @Default.String("bar")
  String getName();

  @Max(100)
  @Min(1)
  @Default.Integer(42)
  Integer getSize();

  @Default.Boolean(false)
  Boolean getEnabled();
}
```

Any config source that defines a value which breaks the constraints has that property ignored, and *Gestalt* falls-back to the next available definition

#### Whole-instance Validation Report
Sometimes it's useful to know whether all the properties of an instance are configured or have defaults (perhaps in unit tests or where user input is possible).

A instance can be checked, returning a ```ValidationResult```

```java
TestConfig configuration = Configurations.definedBy(TestConfig)
      .fromPropertiesResource("common.properties")
      .done();

ValidationResult validationResult = Configurations.validate(configuration, TestConfig.class);

assertTrue(validationResult.hasFailures());
for(ValidationResult.Item item : validationResult) {
    ...
}

```

### Disabling Features

*Gestalt* features can be switched-off e.g. falling back to defaults and throwing an exception for undefined values can be disabled by calling

```java
Configuration.without(Feature.Defaults, Feature.ExceptionOnNullValue)...
```

The switchable features are

* ```Defaults``` - don't use declared default values
* ```Validation``` - don't validate values
* ```Caching``` - by default the result of every lookup is cached, this switches it off
* ```ExceptionOnNullValue``` - if disabled returns ```null``` if a value is undefined instead

> **WARNING**: If the configuration interfaces define methods which return primitives, and if
> ```ExceptionOnNullValue``` is switched off, then a non-obvious exception is thrown anyway for
> any attempt to access the undefined primitive value!
>
> Workarounds include switching to the object wrappers (e.g. boolean -> Boolean) and defining defaults

### Persisting

An existing instance of the config-interface can be turned into the appropriate XML-string (ready to be persisted through the mechanism of your choice)

```java
Configurations.serialise(configInstance, SimpleConfig.class).toXml();
```

would produce something like
```xml
<SimpleConfig>
  <level>42</level>
  <enabled>true</enabled>
  <name>bar</name>
</SimpleConfig>
```

A `String` in the correct format for saving to a `Properties` file instance can also be used for persistence

```java
String propsString = Configurations.serialise(configInstance, SimpleConfig.class).toProperties();
```

... and a ```Properties``` instance can be used to build a Configuration instance

```java
 SimpleConfig config = Configurations.definedBy(SimpleConfig.class).fromProperties(props)
```

One use-case for this would be to load/save ```Properties``` instances from/to a database, thereby allowing a Configuration instance to be stored and retrieved.

#### Persistence with Comments
Both XML and Properties serialisations allow comments to be added

```java
String xmlString = Configurations.serialise(configInstance, SimpleConfig.class).withComments().toXml()
```

Might produce

```xml
<SimpleConfig>
  <level>42</level><!-- level: [[Size: min=5, max=100], default 42] -->
  <enabled>true</enabled>
  <name>bar</name><!-- name: [What's in a a name?, Not Null] -->
</SimpleConfig>
```

```java
String propsString = Configurations.serialise(configInstance, SimpleConfig.class).withComments().toProperties()
```

Might produce

```properties
# this.subLevel.name: [The name of the sub-level]
thing.subLevel.name='foo'
# thing.aDifferentSection.size: [[Size: min=5, max=100], default 42]
thing.aDifferentSection.size=7
```

The comments are generated from annotations in the `interface` where available
* A specific `@Comment`
* Any validation constraint
* A `@Default`

#### Persistence with Customised Naming
When serialising to/from XML or Properties files it may be more idiomatic to generate and read e.g. ```Properties``` files of the form

```properties
thing.sub-level.name='foo'
thing.a-different-section.size=7
```

or

```xml
<thing>
  <some-sub-config>42</some-sub-config>
</thing>
```

rather than the default ```camelCase```.

By specifying a ```PropertyNameTransformer``` implementation (just a single method ```String fromPropertyName(String propertyName)```) when serialising **and de-serialising** the results can be customised.

E.g. Using the supplied ```HyphenatedPropertyNameTransformer```

```java
Properties props = Configurations.serialise(configInstance, SimpleConfig.class).using(new HyphenatedPropertyNameTransformer()).toProperties()
...
SimpleConfig config = Configurations.definedBy(SimpleConfig.class).fromProperties(props, new HyphenatedPropertyNameTransformer())
```

## Custom Types
There may be circumstances where you want to use a concrete type instead of an interface for a property value
(e.g. [tiny-types](http://darrenhobbs.com/2007/04/11/tiny-types/)), by following a couple of rules this is possible

**Rule 1:** a factory-method named `fromString` taking a single `String` argument e.g.

```java
public class MyClass {
  ...
  public static MyClass fromString(String stringValue) { ... }
  ...
}
```

> **NOTE** there's no need to implement a specific interface, which isn't possible with static methods anyway, since
> **Gestalt** uses Groovy duck-typing to make the call.

**Rule 2:** override `toString()` to return a suitable `String` serialisation of your type

Careful readers will spot that this means that `fromString(...)` and `toString()` need to be symmetric otherwise there
will be problems if you read-in/write-out configuration instances.

### An Example Custom Type - ```ObfuscatedString```
As an example of how to write a custom type, see [`ObfuscatedString`](src/main/java/com/github/tonybaines/gestalt/ObfuscatedString.java)
and it's [tests](src/test/groovy/com/github/tonybaines/gestalt/ObfuscatedStringSpec.groovy)

Use it in the same way as any other config property as part of an enclosing `interface`

```java
public interface ConfigWithObfuscatedString {
  ObfuscatedString getS3cret();
}
```

To read the plaintext version in your application, the `ObfuscatedString` type has a method `toPlainTextString()`.


## See the specifications for more

The features described above (and more) were developed from the specifications in [src/test/groovy/com/github/tonybaines/gestalt](src/test/groovy/com/github/tonybaines/gestalt/) , using the example config sources in [src/test/resources](src/test/resources).
Please read the descriptions and bodies of the tests to see patterns of use and expected behaviour.
