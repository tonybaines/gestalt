package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.Handed
import com.github.tonybaines.gestalt.TestConfig
import com.github.tonybaines.gestalt.transformers.HyphenatedPropertyNameTransformer
import spock.lang.Specification

class SystemPropertiesConfigSourceSpec extends Specification {
  def  "Accessing properties"() {
    given:
    System.setProperty("TEST.intValue", "42")
    System.setProperty("TEST.stringValue", "Fourty Two")
    System.setProperty("TEST.doubleValue", "42.2")
    System.setProperty("TEST.longValue", "42000000")
    System.setProperty("TEST.booleanValue", "false")
    System.setProperty("TEST.handedness", "right")
    System.setProperty("TEST.subConfig.intValue", "43")
    System.setProperty("TEST.strings.0", "foo")
    System.setProperty("TEST.strings.1", "bar")

    when:
    def config = Configurations.definedBy(TestConfig)
        .fromSystemProperties("TEST")
        .fromPropertiesResource('common.properties')
        .done()

    then:
    config.intValue == 42
    config.stringValue == "Fourty Two"
    config.doubleValue == 42.2
    config.longValue == 42000000L
    config.booleanValue == false
    config.handedness == Handed.right
    config.subConfig.intValue == 43
    config.strings.containsAll(['foo', 'bar'])
  }

  def  "Accessing properties with a property-name transformer"() {
    given:
    System.setProperty("TEST.int-value", "42")
    System.setProperty("TEST.string-value", "Fourty Two")
    System.setProperty("TEST.double-value", "42.2")
    System.setProperty("TEST.long-value", "42000000")
    System.setProperty("TEST.boolean-value", "false")
    System.setProperty("TEST.handedness", "right")
    System.setProperty("TEST.sub-config.int-value", "43")

    when:
    def config = Configurations.definedBy(TestConfig)
        .fromSystemProperties("TEST", new HyphenatedPropertyNameTransformer())
        .fromPropertiesResource('common.properties')
        .done()

    then:
    config.intValue == 42
    config.stringValue == "Fourty Two"
    config.doubleValue == 42.2
    config.longValue == 42000000L
    config.booleanValue == false
    config.handedness == Handed.right
    config.subConfig.intValue == 43
  }
}