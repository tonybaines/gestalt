package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import groovy.util.logging.Slf4j

import javax.validation.constraints.NotNull

@Slf4j
class GroovyConfigSource extends BaseConfigSource {

  GroovyConfigSource(@NotNull InputStream inputStream, PropertyNameTransformer propertyNameTransformer, constants) {
    super.config = new ConfigSlurper().parse(inputStream.text).values().first()
    super.constants = constants
    super.propertyNameTransformer = propertyNameTransformer
  }

  private GroovyConfigSource(Object node, PropertyNameTransformer propertyNameTransformer, constants) {
    super(node, propertyNameTransformer, constants)
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new GroovyConfigSource(node, super.propertyNameTransformer, constants)
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
