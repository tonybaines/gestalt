package com.github.tonybaines.gestalt

import spock.lang.Specification

class BugReportSpec extends Specification {
  public interface Eventing {
    MetricsConfig getMetrics()
  }
  interface MetricsConfig {
//    @Default.Boolean(false)
    boolean isJvmGaugesEnabled();
  }

  def "Issue 12: NPE when accessing a missing primitive value"() {
    given:
    Eventing configInstance = Configurations.definedBy(Eventing)
      .without(Configurations.Feature.Validation)
      .without(Configurations.Feature.ExceptionOnNullValue)
      .fromXml(new ByteArrayInputStream('<Eventing/>'.bytes))
      .done()

    when:
    configInstance.metrics.isJvmGaugesEnabled() == null

    then:
    thrown(NullPointerException)
  }
}