package tonybaines.configuration

import groovy.util.logging.Slf4j

import java.lang.reflect.Method

@Slf4j
class XmlConfigSource implements ConfigSource {
  Node xml

  XmlConfigSource(String filePath) {
    xml = new XmlParser().parse(this.class.classLoader.getResourceAsStream(filePath))
  }

  private XmlConfigSource(Node node) {
    xml = node
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      def node = path.inject(xml) { acc, val -> acc."$val" }
      if (node.size() > 1) throw new ConfigurationException(method.name, "more than one definition")

      if (method.returnType.enum) return method.returnType.valueOf(valueOf(node))
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
      case Integer: return valueOf(node).toInteger()
      case Double: return valueOf(node).toDouble()
      case Boolean: return valueOf(node).toBoolean()

      default: new DynoClass(new XmlConfigSource(node)).getMapAsInterface(returnType)
    }
  }

  protected String valueOf(node) {
    node.text()
  }

  protected handleList(node, method) {
    node[0].collect { child ->
      decoded(child, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
