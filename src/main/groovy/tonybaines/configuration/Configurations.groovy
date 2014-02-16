package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/*
 TODO: try building up paths e.g. ['config', 'strings'] and evaluating against the underlying store
 TODO: Filtering is the responsibility of client code, not config. 'id' is NOT a special case
  */

abstract class Configurations<T> {

  static Factory<T> definedBy(Class configInterface) {
    new Factory(configInterface)
  }

  abstract <T> T load()

  static class Factory<T> {
    Class configInterface

    Factory(configInterface) {
      this.configInterface = configInterface
    }

    public <T> Configurations<T> fromXmlFile(String filePath) {
      new XmlConfigurations(configInterface, filePath)
    }

    public <T> Configurations<T> fromPropertiesFile(String filePath) {
      new PropertiesConfigurations(configInterface, filePath)
    }
  }

  static class PropertiesConfigurations<T> extends Configurations<T> {
    private Class configInterface
    private InputStream source

    private PropertiesConfigurations(Class configInterface, String filePath) {
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

  static class XmlConfigurations<T> extends Configurations<T> {
    private Class configInterface
    private InputStream source

    private XmlConfigurations(Class configInterface, String filePath) {
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

}
