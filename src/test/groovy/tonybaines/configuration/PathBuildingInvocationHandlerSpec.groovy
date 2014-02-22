package tonybaines.configuration

import spock.lang.Specification

class PathBuildingInvocationHandlerSpec extends Specification {
  def "the path to top-level properties is a single item list"() {
    given:
    ConfigSource source = Mock()
    TestConfig config = new PathBuildingInvocationHandler(source).around(TestConfig.class)

    when:
    config.getBooleanValue()
    config.getIntValue()

    then:
    1 * source.lookup(['booleanValue'])
    1 * source.lookup(['intValue'])
  }

  def "the path to lower-level properties is a multi item list"() {
    given:
    ConfigSource source = Mock()
    TestConfig config = new PathBuildingInvocationHandler().around(TestConfig.class, source)

    when:
    config.getSubConfig().getIntValue()

    then:
    1 * source.lookup(['subConfig', 'intValue'])
  }
}