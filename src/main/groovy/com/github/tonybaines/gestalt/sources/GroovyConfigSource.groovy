package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import groovy.util.logging.Slf4j

import javax.validation.constraints.NotNull

@Slf4j
class GroovyConfigSource extends BaseConfigSource {

  GroovyConfigSource(@NotNull InputStream inputStream, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, constants) {
    super.config = new ConfigSlurper().parse(inputStream.text).values().first()
    super.propertyNameTransformer = propertyNameTransformer
    super.propertyTransformer = propertyTransformer
    super.constants = constants
  }

  private GroovyConfigSource(Object node, PropertyNameTransformer propertyNameTransformer, PropertyTypeTransformer propertyTransformer, constants) {
    super(node, propertyNameTransformer, propertyTransformer, constants)
  }

  @Override
  protected ConfigSource newInstanceAround(node, constants) {
    new GroovyConfigSource(node, super.propertyNameTransformer, super.propertyTransformer, constants)
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
