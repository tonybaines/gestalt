package com.github.tonybaines.gestalt

import issue10.Eventing
import spock.lang.Specification

class BugReportSpec extends Specification {

  def "Issue ???????: NPE when accessing a missing value"() {
    expect:
    Eventing configInstance = Configurations.definedBy(Eventing)
      .without(Configurations.Feature.Validation)
      .without(Configurations.Feature.ExceptionOnNullValue)
      .fromXmlResource('issue-10.xml')
      .done()

    configInstance.metrics == null
    configInstance.metrics.jvmGaugesEnabled == null

  }
}