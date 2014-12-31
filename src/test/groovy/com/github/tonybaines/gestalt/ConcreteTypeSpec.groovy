package com.github.tonybaines.gestalt

import spock.lang.Specification

import static com.github.tonybaines.gestalt.Configurations.Utils.propsFromString

class ConcreteTypeSpec extends Specification {

  def "Can create a config instance containing a concrete type"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'

    when:
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    then:
    configInstance.getConcrete().toString() == 'foo'
  }

  def "Can serialise a config instance with a concrete type to Properties"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    when:
    Properties serialisedProps = propsFromString(Configurations.serialise(configInstance, ConfigWithAConcreteType).toProperties())

    then:
    serialisedProps.'concrete' == 'foo'
  }

  def "Can serialise a config instance with a concrete type to XML"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'
    ConfigWithAConcreteType configInstance = Configurations.definedBy(ConfigWithAConcreteType).fromProperties(props).done()

    when:
    String xmlString = Configurations.serialise(configInstance, ConfigWithAConcreteType).toXml()
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.concrete == 'foo'
  }

  def "When a concrete type doesn't define fromString(...) accessing the instance fails"() {
    given:
    Properties props = new Properties()
    props.'concrete' = 'foo'

    when:
    ConfigWithABadConcreteType configInstance = Configurations.definedBy(ConfigWithABadConcreteType).fromProperties(props).done()
    configInstance.getConcrete()

    then:
    def e = thrown(ConfigurationException)
    e.message.startsWith("Can't handle non-interface types that don't declare a fromString() factory method")
  }

  def "Works with the ObfuscatedString concrete type"() {
    given:
    Properties props = new Properties()
    props.'s3cret' = 'password123'

    when:
    ConfigWithObfuscatedString configInstance = Configurations.definedBy(ConfigWithObfuscatedString).fromProperties(props).done()

    then:
    configInstance.getS3cret().toString() == '{rot13}cnffjbeq123'
    configInstance.getS3cret().toPlainTextString() == 'password123'
  }

  interface ConfigWithObfuscatedString {
    ObfuscatedString getS3cret()
  }

  interface ConfigWithAConcreteType {
    ConcreteType getConcrete()
  }

  interface ConfigWithABadConcreteType {
    BadConcreteType getConcrete()
  }

  static class BadConcreteType {
  }

  static class ConcreteType {
    private String value

    /** Factory method */
    public static ConcreteType fromString(String value) {
      return new ConcreteType(value)
    }

    private ConcreteType(String value) { this.value = value }
    @Override String toString() { value }
  }
}