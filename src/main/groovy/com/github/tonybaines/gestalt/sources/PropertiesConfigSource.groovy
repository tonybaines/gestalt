package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import groovy.util.logging.Slf4j


@Slf4j
class PropertiesConfigSource extends BaseConfigSource {

  PropertiesConfigSource(InputStream inputStream, constants) {
    if (inputStream == null) throw new ConfigurationException('Null input source')
    def propsFile = new Properties()
    propsFile.load(inputStream)
    super.config = new ConfigSlurper().parse(propsFile)
    super.constants = constants
  }

  private PropertiesConfigSource(Object node, constants) {
    super(node, constants)
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new PropertiesConfigSource(node, constants)
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
