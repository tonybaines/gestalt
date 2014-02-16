package tonybaines.configuration

import spock.lang.Specification

import javax.validation.Validation
import javax.validation.ValidatorFactory

class ValidationSpec extends Specification {
  ValidatorFactory vf = Validation.buildDefaultValidatorFactory()

  def "invalid values throw an exception"() {
    given:
    def badConfig = new DefaultTestConfig()
    def validator = vf.getValidator()


    when:
    def violations = validator.validate(badConfig)

    then:
    violations.size() == 1
  }

  class DefaultTestConfig implements TestConfig {
    Integer getIntValue() { return null }

    String getStringValue() { return null }

    Double getDoubleValue() { return null }

    Boolean getBooleanValue() { return null }

    Handed getHandedness() { return null }

    TestConfig.SubConfigLevel1 getSubConfig() { return null }

    List<String> getStrings() { return null }

    List<TestConfig.Thing> getThings() { return null }
  }

}