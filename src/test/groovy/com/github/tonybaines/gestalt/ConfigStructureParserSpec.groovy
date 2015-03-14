package com.github.tonybaines.gestalt

import spock.lang.Specification


class ConfigStructureParserSpec extends Specification {

  def "A config structure with simple properties"() {
    given:
    ConfigStructureObserver observer = Mock()
    def parser = new ConfigStructureParser(observer)

    when: "parsed"
    parser.parse(SimpleConfig.class)

    then: "generates an event for each property"
    1 * observer.property(new ConfigProperty('name', String))
    1 * observer.property(new ConfigProperty('level', Integer))
    1 * observer.property(new ConfigProperty('enabled', Boolean))
    1 * observer.property(new ConfigProperty('defaultOnly', String))
  }

}