package com.github.tonybaines.gestalt

import spock.lang.Specification

import javax.validation.ConstraintViolation

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
      .without(Configurations.Feature.Validation, Configurations.Feature.ExceptionOnNullValue)
      .done()

    when:
    Set<ConstraintViolation> validationResult = Configurations.validate(configuration, TestConfig)
    validationResult.each {
      println "${it.propertyPath}: ${it.message}"
    }

    then:
    validationResult.any { it.propertyPath.toString() == 'stringValueWhoseDefaultBreaksValidation' }
    validationResult.any { it.propertyPath.toString() == 'integerThatIsTooLarge' }
  }
}