package tonybaines.configuration

import spock.lang.Specification

class PathBuildingInvocationHandlerSpec extends Specification {
  def "the path to top-level properties is a single item list"() {
    given:
    ConfigSource source = Mock()
    TestConfig config = java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [TestConfig.class], new PathBuildingInvocationHandler(source))

    when:
    config.getBooleanValue()

    then:
    1 * source.lookup(['booleanValue'])
  }
}