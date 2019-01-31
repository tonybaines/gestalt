package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.transformers.HyphenatedPropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.google.common.collect.Lists
import spock.lang.Specification

import static com.github.tonybaines.gestalt.Configurations.Utils.propsFromString

class PersistenceSpec extends Specification {

  def "A config-interface instance can be transformed into an XML string"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    def xmlString = Configurations.serialise(configInstance, TestConfig).toXml()
    println xmlString
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.intValue == 1
    xml.handedness == 'left'
    xml.doubleValue == 42.5
    xml.booleanValue == false
    xml.allTheThings.children().any { thing ->
      thing.id == "123abc" &&
        thing.stringValue == "foo"
    }
    xml.subConfig.intValue == 42
    xml.strings.children().size() == 2

  }

  def "An instance created from an external source can be persisted [XML]"() {
    given: 'a config instance'
    SimpleConfig fromString = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(STATIC_XML.bytes)).done()

    when: "it's turned into a String and re-parsed"
    def xmlString = Configurations.serialise(fromString, SimpleConfig).toXml()
    println xmlString
    SimpleConfig roundTripped = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(xmlString.bytes)).done()

    then:
    fromString.name == roundTripped.name
    fromString.level == roundTripped.level
    fromString.enabled == roundTripped.enabled

  }

  def "An instance created from an external source can be persisted [Properties]"() {
    given: 'a config instance'
    TestConfig configInstance = aNewConfigInstance()

    when: "it's turned into a String and re-parsed"
    def props = propsFromString(Configurations.serialise(configInstance, TestConfig).toProperties())
    TestConfig roundTripped = Configurations.definedBy(TestConfig).fromProperties(props).done()

    then:
    roundTripped.intValue == 1
    roundTripped.handedness == Handed.left
    roundTripped.doubleValue == 42.5
    roundTripped.booleanValue == false
    roundTripped.subConfig.intValue == 42
    roundTripped.allTheThings[0].id == "123abc"
    roundTripped.allTheThings[0].stringValue == "foo"
    roundTripped.strings[0] == "foo"
    roundTripped.strings[1] == "bar"
    roundTripped.subConfig.l2.level3Property == 'baz'

  }

  def "An instance created from a mutable implementation of the Config interface can be persisted"() {
    given:
    UpdateableSimpleConfig config = new UpdateableSimpleConfig()
    config.setName("arthur")
    config.setLevel(-1)
    config.setEnabled(true)

    when:
    def xmlString = Configurations.serialise(config, SimpleConfig).toXml()
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.name == 'arthur'
    xml.level == -1
    xml.enabled == true
  }

  def "A config-interface instance can be transformed into a Properties instance"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    Properties props = propsFromString(Configurations.serialise(configInstance, TestConfig).toProperties())

    then:
    props.intValue == '1'
    props.handedness == 'left'
    props.doubleValue == '42.5'
    props.booleanValue == 'false'
    props.'subConfig.intValue' == '42'
    props.'allTheThings.0.id' == "123abc"
    props.'allTheThings.0.stringValue' == "foo"
    props.'strings.0' == "foo"
    props.'strings.1' == "bar"
    props.'subConfig.l2.level3Property' == 'baz'
  }

  def "A Properties instance created from an instance can be converted back into an instance"() {
    given: 'a config instance'
    SimpleConfig fromString = Configurations.definedBy(SimpleConfig).fromXml(new ByteArrayInputStream(STATIC_XML.bytes)).done()

    when: "it's turned into a String and re-parsed"
    def props = propsFromString(Configurations.serialise(fromString, SimpleConfig).toProperties())
    def roundTripped = Configurations.definedBy(SimpleConfig).fromProperties(props).done()

    then:
    roundTripped.name == "bar"
    roundTripped.level == 42
    roundTripped.enabled == true

  }

  def "Allowing property names to be customised when round-tripping via Properties"() {
    given:
    TestConfig configInstance = aNewConfigInstance()
    PropertyNameTransformer transformer = new HyphenatedPropertyNameTransformer()

    when:
    Properties props = propsFromString(Configurations.serialise(configInstance, TestConfig).using(transformer).toProperties())
    TestConfig roundTripped = Configurations.definedBy(TestConfig).fromProperties(props, transformer).done()
    println props

    then:
    props.'double-value' == roundTripped.doubleValue.toString()
    props.'sub-config.int-value' == roundTripped.subConfig.intValue.toString()

  }

  def "Allowing property names to be customised when round-tripping via XML"() {
    given:
    TestConfig configInstance = aNewConfigInstance()
    PropertyNameTransformer transformer = new HyphenatedPropertyNameTransformer()

    when:
    String xmlString = Configurations.serialise(configInstance, TestConfig).using(transformer).toXml()
    TestConfig roundTripped = Configurations.definedBy(TestConfig).fromXml(new ByteArrayInputStream(xmlString.bytes), transformer).done()
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.'double-value' == roundTripped.doubleValue.toString()
    xml.'sub-config'.'int-value' == roundTripped.subConfig.intValue.toString()
    xml.'all-the-things'.children().any { thing ->
      thing.id == "123abc" &&
        thing.'string-value' == "foo"
    }

  }

  def "Customising serialisation with comments (XML)"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    String xmlString = Configurations.serialise(configInstance, TestConfig).withComments().toXml()

    then:
    xmlString.contains('<!-- intValue: [[Not Null]] -->')
  }

  def "Customising serialisation with comments (Properties)"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    String propsString = Configurations.serialise(configInstance, TestConfig).withComments().toProperties()

    then:
    propsString.contains('# intValue: [[Not Null]]')
  }

  def "Round trip serialisation to a Properties file"() {
    given:
    TestConfig configInstance = aNewConfigInstance()
    File tempFile = File.createTempFile('config', '.properties')
    String propString = Configurations.serialise(configInstance, TestConfig).withComments().toProperties()

    when:
    tempFile.text = propString
    TestConfig configFromPropsFile = Configurations.definedBy(TestConfig).fromProperties(new FileInputStream(tempFile)).done()

    then:
    configFromPropsFile.intValue == 1
  }

  def "Round trip serialisation to an XML file"() {
    given:
    TestConfig configInstance = aNewConfigInstance()
    File tempFile = File.createTempFile('config', '.xml')
    String xmlString = Configurations.serialise(configInstance, TestConfig).withComments().toXml()

    when:
    tempFile.text = xmlString
    TestConfig configFromXmlFile = Configurations.definedBy(TestConfig).fromXml(new FileInputStream(tempFile)).done()

    then:
    configFromXmlFile.intValue == 1
  }

  def "Issue #8: a properties object from an instance with a null value"() {
    given:
    SimpleConfig config = new SimpleConfig() {
      String name = null
      Integer level = null
      Boolean isEnabled(){null}
      String defaultOnly = null
    }

    when:
    Properties roundTripped = propsFromString(Configurations.serialise(config, SimpleConfig).toProperties())

    then:
    roundTripped.name == null
    roundTripped.level == null
    roundTripped.enabled == null
    roundTripped.defaultOnly == null
  }

  def "Issue #10: NPE when serialising a config instance with null or missing values (Properties)"() {
    given:
    A configInstance = new A() {
      B getB() { return new B() {
        C getC() { return null }
      } }
    }
    when:
    Properties properties = propsFromString(Configurations.serialise(configInstance, A).toProperties())

    then:
    properties.'b.c' == null

  }

  interface CustomSerialisationConfig {
    @Comment("The address")
    Inet4Address getAddress()
  }
  static class CustomTransformations {
    static Inet4Address to(String s) {
      InetAddress.getByName(s)
    }

    static String from(Inet4Address i) {
      i.toString().replace('/', '')
    }
  }
  def "Persisting with custom serialisation"() {
    given:
    Properties props = ['address': '192.168.0.1']
    CustomSerialisationConfig config = Configurations.definedBy(CustomSerialisationConfig)
      .fromProperties(props)
      .withPropertyTransformer(CustomTransformations.class)
      .done()

    when:
    def builder = Configurations
            .serialise(config, CustomSerialisationConfig)
            .withPropertyTransformer(CustomTransformations.class)
            .withComments()
    Properties roundTripped = propsFromString(builder.toProperties())
    def xml = new XmlSlurper().parseText(builder.toXml())

    then:
    roundTripped['address'] == props['address']
    xml.address == props['address']
  }


  private class UpdateableSimpleConfig implements SimpleConfig {
    private String name
    private int level
    private boolean enabled

    @Override
    String getName() { this.name }

    void setName(String name) { this.name = name }

    @Override
    Integer getLevel() { this.level }

    void setLevel(int level) { this.level = level }

    @Override
    Boolean isEnabled() { this.enabled }

    void setEnabled(boolean enabled) { this.enabled = enabled }

    @Override
    String getDefaultOnly() { return "" }
  }

  private def STATIC_XML = """
<simpleConfig>
  <name>bar</name>
  <enabled>true</enabled>
</simpleConfig>
"""


  TestConfig aNewConfigInstance() {
    new TestConfig() {
      Integer getIntValue() { 1 }

      String getStringValue() { "foo" }

      double getDoubleValue() { 42.5 }

      long getLongValue() { 42000000000 }

      Boolean isBooleanValue() { false }

      Handed getHandedness() { Handed.left }

      List<String> getMissing() { null }

      TestConfig.SubConfigLevel1 getSubConfig() {
        new TestConfig.SubConfigLevel1() {
          Integer getIntValue() { 42 }

          boolean getBooleanValueWhoseValueBreaksValidation() { false }

          String getValueWhichIsDefinedToBreakValidationButHasADefault() { "bar" }

          TestConfig.SubConfigLevel2 getL2() { return new TestConfig.SubConfigLevel2() {
            String getLevel3Property() {
              return "baz"
            }

            String getNonExistent() {
              return null
            }
          } }
        }
      }

      List<String> getStrings() { Lists.newArrayList("foo", "bar") }

      List<TestConfig.SomeThing> getAllTheThings() {
        Lists.newArrayList(
          new TestConfig.SomeThing() {
            String getId() { "123abc" }

            String getStringValue() { "foo" }
          })
      }

      Integer getNonExistent() { null }

      Integer getDeclaredAsAnIntegerButIsAString() { null }

      String getSomethingDefinedTwice() { null }

      String getNonExistentStringWithDefault() { null }

      Integer getNonExistentIntegerWithDefault() { null }

      Boolean getNonExistentBooleanWithDefault() { null }

      Double getNonExistentDoubleWithDefault() { null }

      Handed getNonExistentEnumWithDefault() { null }

      Handed getDefaultedValueWithBadDefinition() { null }

      String getPropertyDefinedOnlyInGroovyConfig() { null }

      String getPropertyDefinedAllConfigSources() { null }

      String getStringValueWhoseDefaultBreaksValidation() { null }

      Integer getIntegerThatIsTooLarge() { null }
    }
  }

}