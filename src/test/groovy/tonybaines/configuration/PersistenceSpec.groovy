package tonybaines.configuration

import com.google.common.collect.Lists
import org.junit.Ignore
import spock.lang.Specification


class PersistenceSpec extends Specification {

  def "A config-interface instance can be transformed into an XML string"() {
    given:
    TestConfig configInstance = aNewConfigInstance()

    when:
    def xmlString = Configurations.toXml(configInstance, TestConfig)
    println xmlString
    def xml = new XmlSlurper().parseText(xmlString)

    then:
    xml.intValue == 1
    xml.handedness == 'left'
    xml.doubleValue == 42.5
    xml.booleanValue == false
    xml.things.children().any { thing ->
      thing.id == "123abc" &&
        thing.stringValue == "foo"
    }
    xml.subConfig.intValue == 42
    xml.strings.children().size() == 2

  }

  @Ignore
  def "A config-interface instance can be transformed into a Properties instance"() {}


  TestConfig aNewConfigInstance() {
    new TestConfig() {
      Integer getIntValue() { 1 }

      String getStringValue() { "foo" }

      Double getDoubleValue() { 42.5 }

      Boolean getBooleanValue() { false }

      Handed getHandedness() { Handed.left }

//      TestConfig.SubConfigLevel1 getSubConfig() {null}
      TestConfig.SubConfigLevel1 getSubConfig() {
        new TestConfig.SubConfigLevel1() {
          Integer getIntValue() { 42 }

          Boolean getBooleanValueWhoseValueBreaksValidation() { false }

          String getValueWhichIsDefinedToBreakValidationButHasADefault() { "bar" }
        }
      }

      List<String> getStrings() { Lists.newArrayList("foo", "bar") }

      List<TestConfig.Thing> getThings() {
        Lists.newArrayList(
          new TestConfig.Thing() {
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