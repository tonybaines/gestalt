package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import groovy.util.logging.Slf4j

@Slf4j
class XmlConfigSource extends BaseConfigSource {
  private Map<String, String> constants = [:]

  XmlConfigSource(InputStream inputStream, constants) {
    this.config = new XmlParser().parse(inputStream)
    this.constants = constants
  }

  private XmlConfigSource(Node node) {
    super(node)
  }

  @Override
  protected handleMultipleNodes(node, method) {
    if (node.size() > 1) throw new ConfigurationException(method.name, "more than one definition")
  }

  @Override
  protected ConfigSource newInstanceAround(node) {
    new XmlConfigSource(node)
  }

  @Override
  protected String valueOf(node) {
    final def expansionRegex = /%\{(.+)\}/

    def value = stringValueOf(node)
    if (value != null && value =~ expansionRegex) {
      def matcher = value =~ expansionRegex
      return constants."${matcher[0][1]}"
    } else return value
  }

  private String stringValueOf(node) {
    if (node == null || node instanceof NodeList && node.isEmpty()) null
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
    // Fall-back to looking up property as an XML attribute
    return decoded(config."@${path.last()}", returnType)
  }
}
