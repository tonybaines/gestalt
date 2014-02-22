package tonybaines.configuration.sources

import groovy.util.logging.Slf4j
import tonybaines.configuration.ConfigSource
import tonybaines.configuration.ConfigurationException
import tonybaines.configuration.Configurations
import tonybaines.configuration.DynoClass

import java.lang.reflect.Method

@Slf4j
class XmlConfigSource implements ConfigSource {
  Node config

  XmlConfigSource(String filePath) {
    config = new XmlParser().parse(this.class.classLoader.getResourceAsStream(filePath))
  }

  private XmlConfigSource(Node node) {
    config = node
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      def node = path.inject(config) { acc, val -> acc."$val" }
      if (node.size() > 1) throw new ConfigurationException(method.name, "more than one definition")
      if (method.returnType.enum) {
        def stringValue = valueOf(node)
        return (stringValue != null) ? method.returnType.valueOf(stringValue) : null
      }
      if (Configurations.isAList(method.genericReturnType)) {
        def list = handleList(node, method)
        return list
      }

      return decoded(node, method.returnType)
    } catch (Throwable e) {
      log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigProxy', '')}"
      throw new ConfigurationException(method, e)
    }
  }

  protected def decoded(node, Class returnType) {
    switch (returnType) {
      case String: return valueOf(node)
      case Integer: return valueOf(node)?.toInteger()
      case Double: return valueOf(node)?.toDouble()
      case Boolean: return valueOf(node)?.toBoolean()

      default: new DynoClass(new XmlConfigSource(node)).getMapAsInterface(returnType)
    }
  }

  protected String valueOf(node) {
    if (node instanceof NodeList && node.isEmpty()) null
    else node.text()
  }

  protected handleList(node, method) {
    node[0].collect { child ->
      decoded(child, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
