package alltehcode.gestalt.sources

import alltehcode.gestalt.ConfigSource
import alltehcode.gestalt.ConfigurationException
import groovy.util.logging.Slf4j

@Slf4j
class XmlConfigSource extends BaseConfigSource {
  XmlConfigSource(String filePath) {
    log.info "Loading XML gestalt from $filePath"
    config = new XmlParser().parse(this.class.classLoader.getResourceAsStream(filePath))
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
    if (node instanceof NodeList && node.isEmpty()) null
    else node.text()
  }

  @Override
  protected handleList(node, method) {
    node[0].collect { child ->
      decoded(child, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
