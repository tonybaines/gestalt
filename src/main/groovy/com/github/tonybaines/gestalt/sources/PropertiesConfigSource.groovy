package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.util.logging.Slf4j


@Slf4j
class PropertiesConfigSource extends BaseConfigSource {

  PropertiesConfigSource(InputStream inputStream, PropertyNameTransformer propertyNameTransformer, constants) {
    if (inputStream == null) throw new ConfigurationException('Null input source')
    def propsFile = new Properties()
    propsFile.load(inputStream)
    super.config = new ConfigSlurper().parse(propsFile)
    super.constants = constants
    super.propertyNameTransformer = propertyNameTransformer
  }

  PropertiesConfigSource(Properties props, PropertyNameTransformer propertyNameTransformer, constants) {
    super.config = new ConfigSlurper().parse(props)
    super.constants = constants
    super.propertyNameTransformer = propertyNameTransformer
  }

  private PropertiesConfigSource(Object node, PropertyNameTransformer propertyNameTransformer, constants) {
    super(node, propertyNameTransformer, constants)
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new PropertiesConfigSource(node, propertyNameTransformer, constants)
  }

  @Override
  protected String valueOf(node) {
    if (node.isEmpty()) return null
    else node
  }

  @Override
  protected handleList(node, method) {
    node.entrySet().sort { it.key }.collect { entry ->
      decoded(entry.value, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
