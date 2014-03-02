package alltehcode.gestalt.sources

import alltehcode.gestalt.ConfigSource
import alltehcode.gestalt.ConfigurationException
import groovy.util.logging.Slf4j


@Slf4j
class PropertiesConfigSource extends BaseConfigSource {

  PropertiesConfigSource(InputStream inputStream) {
    if (inputStream == null) throw new ConfigurationException(['Null input source'])
    def propsFile = new Properties()
    propsFile.load(inputStream)
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
