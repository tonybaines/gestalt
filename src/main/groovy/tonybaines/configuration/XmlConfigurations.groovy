package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class XmlConfigurations<T> extends Configurations<T> {
  private Class configInterface
  private InputStream source

  public XmlConfigurations(Class configInterface, String filePath) {
    this.configInterface = configInterface
    source = Configurations.class.classLoader.getResourceAsStream(filePath)
  }

  public <T> T load() {
    def xml = new XmlParser().parse(source)
    return XmlConfigProxy.around(configInterface, xml) as T
  }

  static class XmlConfigProxy implements InvocationHandler {
    def xml

    static <T> T around(Class configInterface, xml) {
      java.lang.reflect.Proxy.newProxyInstance(Configurations.class.classLoader, (Class[]) [configInterface], new XmlConfigProxy(xml))
    }

    XmlConfigProxy(xml) {
      this.xml = xml
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        def nodes = xml."${method.name}"
        def node = nodes[0]

        if (isAList(method.genericReturnType)) {
          return node.children().collect { child ->
            decoded(child, method.genericReturnType.actualTypeArguments[0])
          }
        } else if (method.returnType.enum) {
          return method.returnType.valueOf(node.text())
        } else {
          return decoded(node, method.returnType)
        }

      } catch (Throwable e) {
        e.printStackTrace()
        throw e
      }
    }

    private static isAList(type) { type instanceof ParameterizedType && type.rawType.isAssignableFrom(List) }

    private def decoded(node, returnType) {
      switch (returnType) {
        case String: return node.text()
        case Integer: return node.text().toInteger()
        case Double: return node.text().toDouble()
        case Boolean: return node.text().toBoolean()

        default: return XmlConfigProxy.around(returnType, node)
      }
    }
  }
}

