package com.github.tonybaines.gestalt

import spock.lang.Specification
import spock.lang.Unroll

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

    where:
    name     | configuration
    'XML'    | Configurations.definedBy(TestConfig).fromXmlResource('common.xml').done()
    'Props'  | Configurations.definedBy(TestConfig).fromPropertiesResource('common.properties').done()
    'Groovy' | Configurations.definedBy(TestConfig).fromGroovyConfigResource('common.grc').done()
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
      .done()

    then:
    config.getIntValue() == 5
  }

  def "Loading a missing configuration source is a not a error if it is optional (Properties)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig)
      .fromPropertiesResource('common.properties')
      .fromPropertiesResource('no-such-resource', isOptional)
      .done()

    then:
    config.getIntValue() == 5
  }

  def "Loading a missing configuration source is a not a error if it is optional (GroovyConfig)"() {
    when:
    TestConfig config = Configurations.definedBy(TestConfig)
      .fromGroovyConfigResource('common.grc')
      .fromGroovyConfigResource('no-such-resource', isOptional)
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
      .done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('No valid sources configured')
  }


  public interface Eventing {
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


}