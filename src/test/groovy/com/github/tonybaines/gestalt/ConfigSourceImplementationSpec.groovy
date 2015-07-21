package com.github.tonybaines.gestalt

import spock.lang.Specification

import javax.validation.Configuration
import java.lang.reflect.Method


/*============================================================================*
 * - COPYRIGHT NOTICE -
 *
 * Copyright (c) British Telecommunications plc, 2010, All Rights Reserved
 *
 * The information contained herein and which follows is proprietary
 * information and is the property of BT. This information is not to be
 * divulged, released, copied, reproduced, or conveyed to unauthorised
 * personnel,companies or other institutions without the direct and expressed
 * approval in writing of BT
 *
 *============================================================================*/

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