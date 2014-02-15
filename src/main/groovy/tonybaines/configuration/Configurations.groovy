package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class Configurations<T> {
  private Class configInterface
  private InputStream source
  private def config

  static Configurations<T> definedBy(Class configInterface) {
    new Configurations<T>(configInterface)
  }

  def fromXmlFile(String filePath) {
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

        default: return ConfigProxy.around(returnType, node)
      }
    }
  }

/*  THIS MIGHT WORK AS AN ALTERNATIVE, HARD TO DEBUG THOUGH
    Binding binding = new Binding()
    binding.setVariable("xml", xml)
    GroovyShell shell = new GroovyShell(binding)
    return shell.evaluate("""
class MyTestConfig implements tonybaines.configuration.TestConfig {
  def xml
  MyTestConfig(xml) {
   this.xml = xml
  }

  public Integer intValue(){ xml.intValue.text().toInteger() }

  public String stringValue(){xml.stringValue.text()}

  public Double doubleValue(){xml.doubleValue.text().toDouble()}

  public tonybaines.configuration.TestConfig.SubConfigLevel1 subConfig() {
    new tonybaines.configuration.TestConfig.SubConfigLevel1() {
      public Integer intValue(){ xml.subConfig.intValue.text().toInteger() }
    } as tonybaines.configuration.TestConfig.SubConfigLevel1
  }
}

return new MyTestConfig(xml)
""") as T*/

}
