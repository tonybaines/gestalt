package com.github.tonybaines.gestalt

import spock.lang.Specification

class BugReportSpec extends Specification {
  interface Truth {
    boolean getBool()
  }

  def "Issue 17: False boolean value is treated as not found"() {
    given:
    Properties props = new Properties()
    props.setProperty('bool', 'false')
    Truth configInstance = Configurations.definedBy(Truth)
      .fromProperties(props)
      .done()

    when:
    configInstance.getBool() == false

    then:
    thrown ConfigurationException

  }
}