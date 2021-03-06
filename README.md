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
    Boolean getBar();
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

  @Default.EmptyList
  List<String> getMissing();
}
```

So a lookup to ```config.getSize()``` when the value isn't explicitly defined will return ```42```

Properties defined as a ```List``` can only be defaulted to an empty list.

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

Multiple sources can be combined from XML, ```.properties``` and [GroovyConfig](http://groovy.codehaus.org/gapi/groovy/util/ConfigSlurper.html) (the __first__ definition found wins)

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

Each type of configuration format can also be accessed as a full file path e.g.
```java
Configurations
  .definedBy(SimpleConfig.class)
  .fromXmlFile("/opt/my-app/etc/config/simple-config.xml")
  .done();
```

File sources can also be flagged with `isOptional`

#### System Properties
Values can be set from Java system properties (`-D` options), which may be helpful in more dynamic environments to 
optionally override certain values.

Properties are expected to be dot-separated with a prefix/namespace, and can make use of a `PropertyNameTransformer`
e.g.

```java
Configurations
  .definedBy(SimpleConfig.class)
  .fromSystemProperties("my-app")
  .fromXmlFile("/opt/my-app/etc/config/simple-config.xml")
  .done();
```

Which would allow the `name` and `enabled` properties to be configured like this;
```
java -jar my-app ... -Dmy-app.name=baz -Dmy-app.enabled=false
```

##### Limitations
Lists of values have limited support; a list of simple types (`String`, '`Integer`, `Boolean` etc) will work 
(e.g. `config.strings.0`), but lists of complex types (e.g. `config.allTheThings.0.myThing.id`) will not 

#### Fallback Configuration File
If there are no (valid) configuration sources defined, before failing, *Gestalt* will attempt to load from
a file `config-class.properties` in the current working directory.  No attempt is made to load other formats
or from other locations.

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

  // for `Boolean` properties (although it works with `boolean`)
  @Default.Boolean(false)
  Boolean isEnabled();
}
```

There's also a Gestalt-specific `@Optional` annotation which suppresses the exception for a property with no value defined (useful for complex either/or custom validation)

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

#### Disabling Caching for a specific interface or property
If a custom source of dynamic property values is used it may be helpful to disable
caching in a more specific way (rather than globaly).

Simply add the `@NoCache` annotation to an interface or method to disable caching.

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
(e.g. [tiny-types](http://darrenhobbs.com/2007/04/11/tiny-types/)),

### Custom Types That You Control
If you have access to the source for a type, following a couple of rules makes this possible

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

#### An Example Custom Type - ```ObfuscatedString```
As an example of how to write a custom type, see [`ObfuscatedString`](src/main/java/com/github/tonybaines/gestalt/ObfuscatedString.java)
and it's [tests](src/test/groovy/com/github/tonybaines/gestalt/ObfuscatedStringSpec.groovy)

Use it in the same way as any other config property as part of an enclosing `interface`

```java
public interface ConfigWithObfuscatedString {
  ObfuscatedString getS3cret();
}
```

To read the plaintext version in your application, the `ObfuscatedString` type has a method `toPlainTextString()`.

### Types You Don't Control

You may want to return a type in your configuration interface that you don't control the source for, e.g. a
`java.net.InetAddress`

```java
public interface InternetAddressConfig {
    InetAddress getAddress();
}
```

Register a source of custom transformations using the `withPropertyTransformer(Class tx)` method
```java
 InternetAddressConfig config = Configurations.definedBy(InternetAddressConfig.class)
                .withPropertyTransformer(CustomTransformations.class)
                .fromProperties(...)
                .done();
```

The custom transformations must be `static` methods which take a `String` and return the required type, the name
of the method isn't significant.
```java
public class CustomTransformations {
    public static InetAddress makeInetAddress(String s) {
        return InetAddress.getByName(s);
    }
}
```

This mechanism works with default values and constants, any exceptions from the transformation are rethrown.  If
there is more than one method with the same return type **all** of them will be ignored (with a logged warning).

#### Persisting Values
If you need to persist configurations to `Properties` or XML you'll need to write a symmetric serialisation function
to produce a `String` from that type such that it can be read back in e.g.
```java
public class CustomTransformations {
    ...
    public static String fromInetAddress(InetAddress i) {
        return i.toString().replace('/', '');
    }
}
```

**N.B.** Once again, it need to be unique and `static`

Specify the transformations library for serialisation in a similar way to importing
```java
String xml = Configurations
                .serialise(config, CustomSerialisationConfig.class)
                .withPropertyTransformer(CustomTransformations.class)
                .withComments()
                .toXml();
```

## Custom `ConfigSource` implementations

To add support for a source of configuration property values from a custom back-end (e.g. a database), supply an
instance of an implementation of [`ConfigSource`](src/main/java/com/github/tonybaines/gestalt/ConfigSource.java)

```java
Configurations.from(customConfigSource).done();
```

A very simple example implementation is in [the test](src/test/groovy/com/github/tonybaines/gestalt/ConfigSourceImplementationSpec.groovy)

## Configuration Interface Instance as a Source

Allows creating or reusing an instance of the config interface as a source in the chain of sources, use-cases
might include; dynamic values (remember to disable caching!), replicated configuration or custom
back-ends.

```java
Configurations.fromConfigInstance(configInstance).done();
```

**N.B.**
* If Validation is enabled the implementation will be called during startup
* The complete interface must be implemented, returning null if a property isn't available through that source


## Custom Validation
Any `default` methods found in a config interface which returns `ValidationResult` or `ValidationResult.Item` will be
called during validation with the configuration object (the same type that the method is defined in).  This gives the
opportunity to access properties at the same level and lower in order to check complex rules that require multiple
values.

```java
default ValidationResult validateNotFooAndBar(CustomValidationConfig instance) {
    ValidationResult result = new ValidationResult();

    if (instance.getFoo() != null && instance.getBar() != null) {
      result.add(ValidationResult.item("foo", "Only Foo *or* Bar should be defined"));
      result.add(ValidationResult.item("bar", "Only Foo *or* Bar should be defined"));
    }

    return result;
  }

  default ValidationResult.Item validateFoo(CustomValidationConfig instance) {
    if ("baz".equals(instance.getFoo()) && "baz".equals(instance.getBaz())) {
      return ValidationResult.item("foo", "foo cannot be 'baz' if baz is also 'baz'");
    }
    return null;
  }
```

## Dynamic Properties
It may be useful for certain configuration values to be dynamic at runtime, rather than
static for the duration of the program e.g. modifying the size of a DB or thread
pool based on metrics such as throughput or machine capacity, or adjusting time-outs
based on an estimate of how much work needs to happen.

The features to support this are
* Configuration Interface Instance as a Source, or Custom `ConfigSource` implementations
* The `@NoCache` annotation

```java
@NoCache
public interface DynamicConfig {
    Long getServiceAHttpTimeout();
    //...
}
```

Custom `ConfigSource`, useful where there are a few dynamic properties in a
larger interface. **Not type-safe**
```java
ConfigSource custom = new ConfigSource() {
    @Override
    public Object lookup(List<String> path, Method method) {
        //... do something clever, return null for unsupported properties
    }
}

Configurations.definedBy(DynamicConfig.class)
    .from(custom)
    .fromPropertiesResource("default-config.properties")
    .done();
```

Configuration Interface as a source, useful when there are a number of dynamic
properties collected into a single (sub)interface.
```java
DynamicConfig configInstance = new DynamicConfig() {
    @Override
    Long getServiceAHttpTimeout() {
        //... do something clever
    }

    // return null for any unsupported methods
}

Configurations.definedBy(DynamicConfig.class)
    .fromConfigInstance(configInstance)
    .fromPropertiesResource("default-config.properties")
    .done();
```

The custom implementation can add extra features as required (e.g. logging changing
values or providing vetoable-changes support).

**N.B.**
The property lookup will happen during startup if validation is enabled, remember to
ensure that any expensive or delayed-availability properties can return safe defaults,
(or null with a file-based config fallback), until they are ready.



## Notes on Working With Kotlin
Using interfaces defined in Java or Groovy should just work, but there may be 
idiomatic advantages to using native Kotlin interfaces

```kotlin
interface KotlinConfig {
    val foo: String
    val bar: KotlinSubConfig
}

interface KotlinSubConfig {
    val baz: Int
}
```

Since properties don't have an exact equivalent in Java some odd-looking syntax is 
requried to use the `@Default` annotations. See [this page](https://kotlinlang.org/docs/reference/annotations.html#annotation-use-site-targets) for more 
information on annotation use-site targets in Kotlin

```kotlin
interface KotlinConfig {
    @get:[Default.String("bar")]
    val foo: String
}
```


## See the specifications for more

The features described above (and more) were developed from the specifications in [src/test/groovy/com/github/tonybaines/gestalt](src/test/groovy/com/github/tonybaines/gestalt/) , using the example config sources in [src/test/resources](src/test/resources).
Please read the descriptions and bodies of the tests to see patterns of use and expected behaviour.
