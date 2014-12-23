package com.github.tonybaines.gestalt

import spock.lang.Specification

class BugReportSpec extends Specification {

  interface A {
    B getB()
  }

  interface B {
    C getC()
  }

  interface C {
    String getFoo()
  }

  def "Issue 10: NPE when serialising a config instance with null/missing values [Properties serialisation]"() {
    given:
    A configInstance = new A() {
      B getB() { return null }
    }
    when:
    Configurations.toProperties(configInstance, A)

    then:
    thrown(NullPointerException)

  }

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