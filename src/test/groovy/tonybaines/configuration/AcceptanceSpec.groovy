package tonybaines.configuration

import spock.lang.Ignore
import spock.lang.Specification


class AcceptanceSpec extends Specification {

  def "Configurations can be queried"() {
    given:
    def configuration = Configurations.definedBy(TestConfig).from('common.xml')

    when:
    TestConfig config = configuration.load()

    then:
    config.intValue() == 5
    config.stringValue() == "Five"
    config.doubleValue() == 5.0
    config.subConfig().intValue() == 6
  }

  def "Configurations are strongly typed"() {
    given:
    def configuration = Configurations.definedBy(TestConfig).from('common.xml')

    when:
    TestConfig config = configuration.load()

    then:
    config.intValue() instanceof Integer
    config.stringValue() instanceof String
    config.doubleValue() instanceof Double
    config.subConfig() instanceof TestConfig.SubConfigLevel1
    config.subConfig().intValue() instanceof Integer
  }

  @Ignore
  def "Missing config elements use the supplied defaults"() {}

  @Ignore
  def "Multiple config elements are an error"() {}

  @Ignore
  def "Unexpected formats are an error"() {}

  @Ignore
  def "Configurations can be saved"() {}

  @Ignore
  def "Configurations can be overridden"() {}

  @Ignore
  def "Changes to persisted stores are reflected without a restart"() {}

}