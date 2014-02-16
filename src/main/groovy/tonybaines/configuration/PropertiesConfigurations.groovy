package tonybaines.configuration

class PropertiesConfigurations<T> extends Configurations<T> {

  public PropertiesConfigurations(Class configInterface, String filePath) {
    super(configInterface, filePath)
  }

  public <T> T load() {
    def propsFile = new Properties()
    propsFile.load(source)
    def props = new ConfigSlurper().parse(propsFile)
    return PropertiesConfigProxy.from(configInterface, props) as T
  }

  static class PropertiesConfigProxy extends Configurations.ConfigurationInvocationHandler {
    def props

    static def from(Class configInterface, props) {
      return new PropertiesConfigProxy(props).around(configInterface, props)
    }

    PropertiesConfigProxy(props) {
      this.props = props
    }

    @Override
    public def around(Class configInterface, props) {
      java.lang.reflect.Proxy.newProxyInstance(Configurations.class.classLoader, (Class[]) [configInterface], new PropertiesConfigProxy(props))
    }

    @Override
    protected String valueOf(node) {
      node
    }

    @Override
    protected handleList(node, method) {
      return node.entrySet().sort { it.key }.collect { entry ->
        decoded(entry.value, method.genericReturnType.actualTypeArguments[0])
      }
    }

    @Override
    protected lookUp(String methodName) {
      props."${methodName}"
    }

  }
}
