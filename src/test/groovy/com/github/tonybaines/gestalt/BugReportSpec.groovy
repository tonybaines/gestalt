package com.github.tonybaines.gestalt

import spock.lang.Specification


class BugReportSpec extends Specification {
  interface Top {
    L1 getL1()
  }

  interface L1 {
    L2 getL2()
    String getProp()
  }

  interface L2 {
    String getProp()
  }

  def "A three-level properties problem"() {
    given:
    Top configInstance = new Top() {
      L1 getL1() {
        return new L1() {
          String getProp() {
            'bar'
          }
          L2 getL2() {
            return new L2() {
              String getProp() {
                'foo'
              }
            }
          }
        }
      }
    }

    when:
    Properties props = Configurations.toProperties(configInstance, Top)

    then:
    configInstance.l1.prop == 'bar'
    configInstance.l1.l2.prop == 'foo'
    props.'l1.prop' == 'bar'
    //props.'l1.l2.prop' == 'foo' // fails
  }

}