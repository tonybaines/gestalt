package com.github.tonybaines.gestalt

import spock.lang.Specification

import java.lang.reflect.Method

class ConfigSourceImplementationSpec extends Specification {

  public static final TestConfig.SomeThing THING_1 = ['id': '1', 'stringValue': 'thing 1'] as TestConfig.SomeThing
  public static final TestConfig.SomeThing THING_2 = ['id': '2', 'stringValue': 'thing 2'] as TestConfig.SomeThing

  def "Consuming a simple externally implemented config source" ( ) {
    given:
    ConfigSource mySource = new ConfigSource() {
      @Override
      def lookup(List<String> path, Method method) {
        switch(path.join('.')) {
          case 'name': return 'myConfigSource'
          case 'level': return 11
          case 'strings': return ['foo', 'bar', 'baz']
        }
      }
    }

    when: ""
    SimpleConfig config = Configurations.definedBy(SimpleConfig).from(mySource).done()

    then: ""
    config.name == 'myConfigSource'
    config.level == 11
  }

  def "Consuming a simple externally implemented config source for a list type" ( ) {
    given:
    ConfigSource mySource = new ConfigSource() {
      @Override
      def lookup(List<String> path, Method method) {
        switch(path.join('.')) {
          case 'strings': return ['foo', 'bar', 'baz']
          case 'allTheThings': return [THING_1, THING_2]
        }
      }
    }

    when: ""
    TestConfig config = Configurations.definedBy(TestConfig).from(mySource).done()

    then: ""
    config.strings.contains('foo')
    config.allTheThings.contains(THING_1)
    config.allTheThings.contains(THING_2)
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
