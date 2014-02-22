package tonybaines.configuration

import java.lang.reflect.Method

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
    def node = path.inject(xml) { acc, val -> acc."$val" }

    if (method.returnType.enum) return method.returnType.valueOf(valueOf(node))
    if (Configurations.isAList(method.genericReturnType)) {
      def list = handleList(node, method)
      return list
    }

    return decoded(node, method.returnType)
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
