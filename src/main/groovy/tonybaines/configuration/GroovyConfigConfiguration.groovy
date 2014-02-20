package tonybaines.configuration

class GroovyConfigConfiguration<T> extends BaseConfiguration<T> {
  String filePath

  public GroovyConfigConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    def groovyConfig = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
    return new ConfigSlurperConfigProxy(groovyConfig).around(configInterface, groovyConfig) as T
  }
}
