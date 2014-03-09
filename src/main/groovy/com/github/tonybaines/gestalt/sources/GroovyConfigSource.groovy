package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import groovy.util.logging.Slf4j

import javax.validation.constraints.NotNull

@Slf4j
class GroovyConfigSource extends BaseConfigSource {

  GroovyConfigSource(@NotNull InputStream inputStream, constants) {
    super.config = new ConfigSlurper().parse(inputStream.text).values().first()
    super.constants = constants
  }

  private GroovyConfigSource(Object node, constants) {
    super(node, constants)
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new GroovyConfigSource(node, constants)
  }

  @Override
  protected String valueOf(node) {
    if (node instanceof ConfigObject && node.isEmpty()) return null
    else node
  }

  @Override
  protected handleList(node, method) {
    node.collect {
      decoded(it, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
