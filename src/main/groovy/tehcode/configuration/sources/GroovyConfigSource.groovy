package tehcode.configuration.sources

import groovy.util.logging.Slf4j
import tehcode.configuration.ConfigSource

@Slf4j
class GroovyConfigSource extends BaseConfigSource {
  GroovyConfigSource(String filePath) {
    log.info "Loading GroovyConfig configuration from $filePath"
    config = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
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