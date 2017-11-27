package com.github.tonybaines.gestalt

import spock.lang.Specification
import spock.lang.Unroll

import javax.validation.constraints.NotNull

import static com.github.tonybaines.gestalt.Configurations.Behaviour.isOptional
import static com.github.tonybaines.gestalt.Fixture.newCompositeConfiguration

class MiscFeaturesSpec extends Specification {

  @Unroll
  def "Undefined values (with no default configured) are an error (#name)"() {
    when:
    TestConfig config = configuration
    config.getSubConfig().getL2().getNonExistent()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getNonExistent')
    e.message.contains('not found in any source')

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
  }

  @Unroll
  def "Missing config elements use the supplied defaults (#name)"() {
    when:
    TestConfig config = configuration

    then:
    config.getNonExistentStringWithDefault() == "default-value"
    config.getNonExistentBooleanWithDefault() == false
    config.getNonExistentIntegerWithDefault() == 42
    config.getNonExistentDoubleWithDefault() == 42.5
    config.getNonExistentEnumWithDefault() == Handed.right
    config.getSubConfig().getL2().getLevel3Property() == 'foobar'

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
  }

  public interface ListWithDefaults {
    @Default.EmptyList
    List<String> getMissing()
  }

  def "Missing list properties can default to being empty"() {
    when:
    ListWithDefaults config = Configurations.definedBy(ListWithDefaults).fromProperties(new Properties()).done()

    println Configurations.serialise(config, ListWithDefaults).toProperties()

    then:
    config.missing != null
    config.missing instanceof List
    config.missing.isEmpty()

  }

  def "Configurations will fall-back until a value is found"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration

    then:
    config.getPropertyDefinedOnlyInGroovyConfig() == 'some-value'
  }

  def "Bad default definitions are an error (when accessed)"() {
    when:
    TestConfig config = newCompositeConfiguration()
    config.getDefaultedValueWithBadDefinition()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getDefaultedValueWithBadDefinition')
    e.cause.message.contains('No enum constant com.github.tonybaines.gestalt.Handed.sideways')
  }

  def "Configurations can be overridden"() {
    given:
    def configuration = newCompositeConfiguration()

    when:
    TestConfig config = configuration

    then:
    config.getPropertyDefinedAllConfigSources() == 'from-properties'
  }

  def "Loading a missing configuration source is a error (XML)"() {
    when:
    Configurations.definedBy(TestConfig).fromXmlResource('no-such-resource').done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Could not load the configuration')
  }

  def "Loading a missing configuration source is a error (Properties)"() {
    when:
    Configurations.definedBy(TestConfig).fromPropertiesResource('no-such-resource').done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Could not load the configuration')
  }

  def "Loading a missing configuration source is a error (GroovyConfig)"() {
    when:
    Configurations.definedBy(TestConfig).fromGroovyConfigResource('no-such-resource').done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Could not load the configuration')
  }

  def "Loading a missing configuration source is not a error if it is optional (XML)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig)
      .fromXmlResource('common.xml')
      .fromXmlResource('no-such-resource', isOptional)
      .fromXmlFile("does-not-exist", isOptional)
      .done()

    then:
    config.getIntValue() == 5
  }

  def "Loading a missing configuration source is a not a error if it is optional (Properties)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig)
      .fromPropertiesResource('common.properties')
      .fromPropertiesResource('no-such-resource', isOptional)
      .fromPropertiesFile("does-not-exist", isOptional)
      .done()

    then:
    config.getIntValue() == 5
  }

  def "Loading a missing configuration source is a not a error if it is optional (GroovyConfig)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig)
      .fromGroovyConfigResource('common.grc')
      .fromGroovyConfigResource('no-such-resource', isOptional)
      .fromGroovyConfigFile('does-not-exist', isOptional)
      .done()

    then:
    config.getIntValue() == 5
  }

  def "No valid configured optional sources is an error"() {
    when:
    Configurations.definedBy(TestConfig)
      .fromXmlResource('no-such-resource', isOptional)
      .fromPropertiesResource('no-such-resource', isOptional)
      .fromGroovyConfigResource('no-such-resource', isOptional)
      .fromXmlFile("does-not-exist", isOptional)
      .fromPropertiesFile("does-not-exist", isOptional)
      .fromGroovyConfigFile('does-not-exist', isOptional)
      .done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('No valid sources available')
  }


  interface Eventing {
    MetricsConfig getMetrics()
  }
  interface MetricsConfig {
    boolean isJvmGaugesEnabled();
  }

  def "Issue 12: NPE when accessing a missing primitive value"() {
    given:
    Eventing configInstance = Configurations.definedBy(Eventing)
      .without(Configurations.Feature.ExceptionOnNullValue)
      .fromXml(new ByteArrayInputStream('<Eventing/>'.bytes))
      .done()

    when:
    configInstance.metrics.isJvmGaugesEnabled() == null

    then:
    thrown(NullPointerException)
  }

  interface Super {
    @NotNull
    @Default.Integer(1)
    Integer getFoo();
  }
  interface Sub extends Super {}

  def "Issue #26 Working with inherited methods"() {
    when:
    def config = Configurations.definedBy(Sub)
            .fromProperties([:] as Properties)
            .done()

    def builder = Configurations.serialise(config, Sub)
    def propsString = builder.toProperties()
    def xmlString = builder.toXml()

    then:
    config.getFoo() == 1
    new XmlSlurper().parseText(xmlString).foo == config.getFoo()
    Configurations.Utils.propsFromString(propsString).get("foo") == config.getFoo().toString()
  }

  static class CustomTransformations {
    static InetAddress makeInetAddress(String s) {
      InetAddress.getByName(s)
    }

    static String trimString(String s) {
      s.trim()
    }
  }
  interface InternetAddressConfig {
    @Default.String("192.168.0.2")
    InetAddress getAddress()

    @NotNull
    String getMultiline()
  }

  def "Property value transformation with a provided function"() {
    when:
    InternetAddressConfig config = Configurations.definedBy(InternetAddressConfig)
            .withPropertyTransformer(CustomTransformations.class)
            .fromProperties(['address': '192.168.0.1', 'multiline': 'foo\nbar\n'] as Properties)
            .done()
    then:
    config.getAddress() != null
    config.getMultiline() == 'foo\nbar'

    Configurations.validate(config, InternetAddressConfig).hasFailures() == false
  }

  def "Property value transformation with a provided function and default value"() {
    when:
    InternetAddressConfig config = Configurations.definedBy(InternetAddressConfig)
            .withPropertyTransformer(CustomTransformations.class)
            .fromProperties([:] as Properties)
            .done()
    then:
    config.getAddress() != null

    Configurations.validate(config, InternetAddressConfig).hasFailures() == true
  }

  def "Property value transformation with a provided function and constant value"() {
    when:
    InternetAddressConfig config = Configurations.definedBy(InternetAddressConfig)
            .withPropertyTransformer(CustomTransformations.class)
            .withConstants(['LOCAL_IP': '192.168.0.3'])
            .fromProperties(['address': '%{LOCAL_IP}'] as Properties)
            .done()
    then:
    config.getAddress() != null
  }

}