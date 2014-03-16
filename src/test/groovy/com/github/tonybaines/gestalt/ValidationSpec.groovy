package com.github.tonybaines.gestalt

import spock.lang.Specification

import static com.github.tonybaines.gestalt.Fixture.newCompositeConfiguration

class ValidationSpec extends Specification {

  def "If the default value is used and it breaks validation constraints, it is ignored (null returned)"() {
    given:
    TestConfig configuration = newCompositeConfiguration()

    when:
    configuration.getStringValueWhoseDefaultBreaksValidation()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getStringValueWhoseDefaultBreaksValidation')
  }

  def "If the default value is used and it breaks validation constraints, it is ignored (null returned) in a sub-interface"() {
    given:
    TestConfig configuration = newCompositeConfiguration()

    when:
    configuration.getStringValueWhoseDefaultBreaksValidation()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('Failed to handle getStringValueWhoseDefaultBreaksValidation')
  }

  def "An invalid configured value in all sources will fall-back to a default"() {
    given:
    TestConfig configuration = newCompositeConfiguration()

    expect:
    configuration.getSubConfig().getValueWhichIsDefinedToBreakValidationButHasADefault() == "fin"
  }

  def "Validating an entire instance"() {
    given:
    TestConfig configuration = Configurations.definedBy(TestConfig)
      .fromPropertiesResource('common.properties')
      .done()

    when:
    ValidationResult validationResult = Configurations.validate(configuration, TestConfig)
    validationResult.each {
      println "${it.property}: ${it.message}"
    }

    then:
    validationResult.hasFailures()
    validationResult.any { it.property == 'TestConfig.stringValueWhoseDefaultBreaksValidation' }
    validationResult.any { it.property == 'TestConfig.integerThatIsTooLarge' }
    validationResult.any { it.property == 'TestConfig.subConfig.booleanValueWhoseValueBreaksValidation' }
  }
}