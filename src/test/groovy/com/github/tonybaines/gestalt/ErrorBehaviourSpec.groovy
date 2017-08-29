package com.github.tonybaines.gestalt

import spock.lang.Specification


class ErrorBehaviourSpec extends Specification {
  def "Error reporting for a missing configuration source"() {
    when: "Reading the config"
    def missingResourceName = "DOES-NOT-EXIST"
    Configurations.definedBy(SimpleConfig).fromPropertiesResource(missingResourceName).done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains(missingResourceName)
  }

  def "Missing values without defaults"() {
    given:
    Properties brokenProps = new Properties()
    def config = Configurations.definedBy(TestConfig).fromProperties(brokenProps).done()

    when:
    config.doubleValue

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getDoubleValue')
    e.stackTrace.length == 0
  }

  def "Property values failing JSR-303 validation"() {
    given:
    Properties brokenProps = new Properties()
    brokenProps['stringValueWhoseDefaultBreaksValidation'] = 'foo'
    def config = Configurations.definedBy(TestConfig).fromProperties(brokenProps).done()

    when:
    config.stringValueWhoseDefaultBreaksValidation

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('getStringValueWhoseDefaultBreaksValidation')
    e.stackTrace.length == 0
  }

  def "No configured sources is a failure"() {
    when:
    Configurations.definedBy(TestConfig).done()

    then:
    def e = thrown(ConfigurationException)
    e.message.contains('No valid sources available')
  }

}