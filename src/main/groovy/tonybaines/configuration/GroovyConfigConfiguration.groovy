package tonybaines.configuration

import groovy.util.logging.Slf4j

@Slf4j
class GroovyConfigConfiguration<T> extends BaseConfiguration<T> {
  String filePath

  public GroovyConfigConfiguration(Class configInterface, String filePath, boolean validateOnLoad) {
    super(configInterface, validateOnLoad)
    this.filePath = filePath
  }

  public T load() {
    log.info "Loading GroovyConfig configuration from $filePath"
    def groovyConfig = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
    T config = new ConfigSlurperConfigProxy(groovyConfig).around(configInterface, groovyConfig) as T
    if (validateOnLoad) Configurations.validate(config)
    config
  }
}
