package alltehcode.gestalt.sources

import alltehcode.gestalt.ConfigSource
import groovy.util.logging.Slf4j

import javax.validation.constraints.NotNull

@Slf4j
class GroovyConfigSource extends BaseConfigSource {

  GroovyConfigSource(@NotNull InputStream inputStream) {
    config = new ConfigSlurper().parse(inputStream.text).values().first()
  }

  private GroovyConfigSource(Object node) {
    super(node)
  }

  @Override
  protected ConfigSource newInstanceAround(node) {
    new GroovyConfigSource(node)
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
