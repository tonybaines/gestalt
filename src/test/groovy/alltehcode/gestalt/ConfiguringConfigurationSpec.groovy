package alltehcode.gestalt

import spock.lang.Ignore
import spock.lang.Specification

import static alltehcode.gestalt.Configurations.Feature.*
import static alltehcode.gestalt.Fixture.newCompositeConfigurationBuilder

class ConfiguringConfigurationSpec extends Specification {

  def "Validation can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(Validation).done()

    expect:
    config.getSubConfig().getValueWhichIsDefinedToBreakValidationButHasADefault() == 'props'
  }

  def "Using defaults can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(Defaults).done()

    when:
    config.getNonExistentBooleanWithDefault()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getNonExistentBooleanWithDefault')
  }

  def "Exceptions on missing lookups can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(ExceptionOnNullValue).done()

    expect:
    config.getNonExistent() == null
  }

  @Ignore('No idea how to verify this without breaking encapsulation')
  def "Caching can be switched-off"() {
    given:
    TestConfig config = newCompositeConfigurationBuilder().without(Caching).done()

    expect:
    config.getSubConfig().getIntValue() == 6
  }
}