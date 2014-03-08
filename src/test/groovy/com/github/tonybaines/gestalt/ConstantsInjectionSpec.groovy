package com.github.tonybaines.gestalt

import spock.lang.Specification

class ConstantsInjectionSpec extends Specification {

  def "constant values from a Map can be injected and used"() {
    given: "a config source that contains references to constants"
    SimpleConfig config = Configurations
      .definedBy(SimpleConfig)
      .fromXmlResource("simple-config-with-constant-refs.xml")
      .withConstants(['BAR': 'bar'])
      .done()

    expect:
    config.name == "bar"

  }

}