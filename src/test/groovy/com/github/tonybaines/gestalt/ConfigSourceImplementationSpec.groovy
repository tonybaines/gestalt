package com.github.tonybaines.gestalt

import spock.lang.Specification

import java.lang.reflect.Method

class ConfigSourceImplementationSpec extends Specification {
  def "Consuming a simple externally implemented config source" ( ) {
    given:
    ConfigSource mySource = new ConfigSource() {
      @Override
      def lookup(List<String> path, Method method) {
        switch(path.join('.')) {
          case 'name': return 'myConfigSource'
          case 'level': return 11
        }
      }
    }

    when: ""
    SimpleConfig config = Configurations.definedBy(SimpleConfig).from(mySource).done()

    then: ""
    config.name == 'myConfigSource'
    config.level == 11
  }

  def "Consuming a complicated externally implemented config source"() {
    given:
    ConfigSource mySource = new ConfigSource() {
      @Override
      def lookup(List<String> path, Method method) {
        switch(path.join('.')) {
          case 'subConfig.l2.level3Property': return 'Forty-two'
        }
      }
    }

    when: ""
    TestConfig config = Configurations.definedBy(TestConfig).from(mySource).done()

    then: "works with defined properties and defaults"
    config.subConfig.l2.level3Property == 'Forty-two'
    config.nonExistentDoubleWithDefault == 42.5
  }
}
