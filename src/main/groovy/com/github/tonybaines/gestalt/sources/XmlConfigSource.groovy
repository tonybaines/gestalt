package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import groovy.util.logging.Slf4j

@Slf4j
class XmlConfigSource extends BaseConfigSource {

  XmlConfigSource(InputStream inputStream, constants) {
    super.config = new XmlParser().parse(inputStream)
    super.constants = constants
  }

  private XmlConfigSource(node, constants) {
    super(node, constants)
  }

  @Override
  protected handleMultipleNodes(node, method) {
    if (node.size() > 1) throw new ConfigurationException(method.name, "more than one definition")
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new XmlConfigSource(node, constants)
  }

  @Override
  protected String valueOf(node) {
    if (node == null || node instanceof NodeList && (node.isEmpty() || node.every { it == null })) null
    else if (node instanceof String) node
    else node.text()
  }

  @Override
  protected handleList(node, method) {
    node[0].collect { child ->
      decoded(child, method.genericReturnType.actualTypeArguments[0])
    }
  }

  @Override
  protected def fallbackLookupStrategy(path, returnType) {
    def node
    // Lookup as far as the penultimate element of the path
    if (path.size() > 1) {
      node = path[0..-2].inject(config) { acc, val -> acc."$val" }
    }
    else {
      node = config
    }
    // then try looking up the property as an XML attribute
    return decoded(node."@${path.last()}", returnType)
  }
}
