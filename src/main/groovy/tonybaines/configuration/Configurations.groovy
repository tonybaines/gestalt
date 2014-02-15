package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class Configurations<T> {
  private Class configInterface
  private InputStream source
  private def config

  static Configurations<T> definedBy(Class configInterface) {
    new Configurations<T>(configInterface)
  }

  def from(String filePath) {
    source = Configurations.class.classLoader.getResourceAsStream(filePath)
    this
  }

  private Configurations(Class configInterface) {
    this.configInterface = configInterface
    config = [:]
  }

  public <T> T load() {
    def xml = new XmlParser().parse(source)
    return ConfigProxy.around(configInterface, xml) as T
  }

  static class ConfigProxy implements InvocationHandler {
    def xml

    static around(Class configInterface, xml) {
      java.lang.reflect.Proxy.newProxyInstance(Configurations.class.classLoader, (Class[]) [configInterface], new ConfigProxy(xml))
    }

    ConfigProxy(xml) {
      this.xml = xml
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      def nodes = xml."${method.name}"
      def node = nodes[0]
      switch (method.returnType) {
        case String: return node.text()
        case Integer: return node.text().toInteger()
        case Double: return node.text().toDouble()

        default: return ConfigProxy.around(method.returnType, xml."${method.name}")
      }
    }
  }

}
