package com.github.tonybaines.gestalt

import issue10.Eventing
import spock.lang.Specification

class BugReportSpec extends Specification {

  def "Issue 10: NPE when serialising a config instance with null/missing values [XML Serialisation]"() {
    given:
    Eventing configInstance = Configurations.definedBy(Eventing)
      .without(Configurations.Feature.Validation)
      .without(Configurations.Feature.ExceptionOnNullValue)
      .fromXmlResource('issue-10.xml')
      .done()
    when:
    println configInstance.getMetrics().class

    String xml = Configurations.toXml(configInstance, Eventing)

    then:
    xml != null

  }
}