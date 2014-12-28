package com.github.tonybaines.gestalt

import spock.lang.Specification
import spock.lang.Unroll

class ConstantsInjectionSpec extends Specification {

  @Unroll
  def "constant values from a Map can be injected and used (#name)"() {
    given: "a config source that contains references to constants"
    EnclosingInterface config = builder
      .withConstants(['NAME': 'bar', 'LEVEL': '11', 'ENABLED': 'true'])
      .done()

    expect:
    config.simpleConfig.name == "bar"
    config.simpleConfig.level == 11
    config.simpleConfig.enabled == true

    where:
    name           | builder
    'XML'          | Configurations.definedBy(EnclosingInterface).fromXmlResource("simple-config-with-constant-refs.xml")
    'Properties'   | Configurations.definedBy(EnclosingInterface).fromPropertiesResource("simple-config-with-constant-refs.properties")
    'GroovyConfig' | Configurations.definedBy(EnclosingInterface).fromGroovyConfigResource("simple-config-with-constant-refs.grc")

  }

}