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
    e.stackTrace.length == 0
  }

}