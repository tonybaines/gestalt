package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import groovy.util.logging.Slf4j

@Slf4j
class XmlConfigSource extends BaseConfigSource {

  XmlConfigSource(InputStream inputStream, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, constants) {
    super.config = new XmlParser().parse(inputStream)
    super.propertyNameTransformer = propertyNameTransformer
    super.propertyTransformer = propertyTransformer
    super.constants = constants
  }

  private XmlConfigSource(node, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, constants) {
    super(node, propertyNameTransformer, propertyTransformer, constants)
  }

  @Override
  protected handleMultipleNodes(node, method) {
    if (node.size() > 1) throw new ConfigurationException(method.name, "more than one definition")
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new XmlConfigSource(node, super.propertyNameTransformer, super.propertyTransformer, constants)
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
