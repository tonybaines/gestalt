package com.github.tonybaines.gestalt

import spock.lang.Specification

class BugReportSpec extends Specification {

  def "Issue 10: NPE when serialising a config instance with null/missing values [XML Serialisation]"() {
    given:
    A configInstance = Configurations.definedBy(A).without(Configurations.Feature.ExceptionOnNullValue, Configurations.Feature.Validation)
      .fromXml(new StringBufferInputStream("""<a><b><c/></b></a>""")).done()
    when:
    Configurations.toXml(configInstance, A)

    then:
    thrown(NullPointerException)

  }
}