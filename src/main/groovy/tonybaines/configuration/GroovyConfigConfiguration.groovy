package tonybaines.configuration

import groovy.util.logging.Slf4j

@Slf4j
class GroovyConfigConfiguration<T> extends BaseConfiguration<T> {
  String filePath

  public GroovyConfigConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    log.info "Loading GroovyConfig configuration from $filePath"
    def groovyConfig = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
    return new ConfigSlurperConfigProxy(groovyConfig).around(configInterface, groovyConfig) as T
  }
}
