# Configuration

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

Using this library can be as simple as

    ```java
    ThingConfig config = (ThingConfig)
        Configurations.definedBy(ThingConfig.class).fromPropertiesFile('thing.properties');

    assertEquals("foo", config.getName());
    assertEquals(7, config.getSize());
    assertEquals(true, config.getEnabled());
    ```

The definition of the ```ThingConfig``` interface drives what properties are expected to be defined in the underlying source files.


### Default Values

By default, an undefined property results in a runtime exception, but we can define defaults in the interface

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

### Multiple sources

Multiple sources can be combined from XML, ```.properties``` and [GroovyConfig](http://groovy.codehaus.org/gapi/groovy/util/ConfigSlurper.html) (last definition wins)

    ```java
    ThingConfig config = (ThingConfig)
      Configurations.definedBy(TestConfig).composedOf()
            .fromPropertiesFile('common.properties')
            .fromXmlFile('common.xml')
            .fromGroovyConfigFile('common.groovy')
            .fromPropertiesFile('user.properties')
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

Any config source that defines a value which breaks the constraints has that property ignored, and teh library falls-back to the next available definition

### Disabling Features

Features can be switched-off e.g. falling back to defaults and throwing an exception for undefined values can be disabled by calling

    ```java
    Configuration.without(Feature.Defaults, Feature.ExceptionOnNullValue)...

    ```

The switchable features are

* Defaults - don't use declared default values
* Validation - don't validate values
* Caching - by default the result of every lookup is cached, this switches it off
* ExceptionOnNullValue - return ```null``` if a value is undefined