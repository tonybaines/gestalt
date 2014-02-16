package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class PropertiesConfigurations<T> extends Configurations<T> {
  private Class configInterface
  private InputStream source

  public PropertiesConfigurations(Class configInterface, String filePath) {
    this.configInterface = configInterface
    source = Configurations.class.classLoader.getResourceAsStream(filePath)
  }

  public <T> T load() {
    def propsFile = new Properties()
    propsFile.load(source)
    def props = new ConfigSlurper().parse(propsFile)
    return PropertiesConfigProxy.around(configInterface, props) as T
  }

  static class PropertiesConfigProxy implements InvocationHandler {
    def props

    static around(Class configInterface, props) {
      java.lang.reflect.Proxy.newProxyInstance(Configurations.class.classLoader, (Class[]) [configInterface], new PropertiesConfigProxy(props))
    }

    PropertiesConfigProxy(props) {
      this.props = props
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        def node = props."${method.name}"

        if (isAList(method.genericReturnType)) {
          return node.entrySet().sort { it.key }.collect { entry ->
            decoded(entry.value, method.genericReturnType.actualTypeArguments[0])
          }
        } else if (method.returnType.enum) {
          return method.returnType.valueOf(node)
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
        case String: return node
        case Integer: return node.toInteger()
        case Double: return node.toDouble()
        case Boolean: return node.toBoolean()

        default: return PropertiesConfigProxy.around(returnType, node)
      }
    }
  }
}
