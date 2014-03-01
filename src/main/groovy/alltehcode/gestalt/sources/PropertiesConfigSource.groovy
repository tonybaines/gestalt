package alltehcode.gestalt.sources

import alltehcode.gestalt.ConfigSource
import groovy.util.logging.Slf4j

@Slf4j
class PropertiesConfigSource extends BaseConfigSource {
  PropertiesConfigSource(String filePath) {
    def propsFile = new Properties()
    log.info "Loading Properties gestalt from $filePath"
    propsFile.load(this.class.classLoader.getResourceAsStream(filePath))
    config = new ConfigSlurper().parse(propsFile)
  }

  private PropertiesConfigSource(Object node) {
    super(node)
  }

  @Override
  protected ConfigSource newInstanceAround(node) {
    new PropertiesConfigSource(node)
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
