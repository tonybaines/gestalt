package tonybaines.configuration

import groovy.util.logging.Slf4j

@Slf4j
class PropertiesConfiguration<T> extends BaseConfiguration<T> {
  String filePath

  public PropertiesConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    def propsFile = new Properties()
    log.info "Loading Properties configuration from $filePath"
    propsFile.load(this.class.classLoader.getResourceAsStream(filePath))
    def props = new ConfigSlurper().parse(propsFile)
    return new PropertiesConfigProxy(props).around(configInterface, props) as T
  }

  static class PropertiesConfigProxy extends ConfigSlurperConfigProxy {

    PropertiesConfigProxy(props) {
      super(props)
    }

    @Override
    public def around(Class configInterface, props) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new PropertiesConfigProxy(props))
    }

    @Override
    protected handleList(node, method) {
      return node.entrySet().sort { it.key }.collect { entry ->
        decoded(entry.value, method.genericReturnType.actualTypeArguments[0])
      }
    }
  }
}
