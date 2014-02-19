package tonybaines.configuration

class PropertiesConfiguration<T> extends DefaultConfiguration<T> {
  String filePath

  public PropertiesConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    def propsFile = new Properties()
    propsFile.load(this.class.classLoader.getResourceAsStream(filePath))
    def props = new ConfigSlurper().parse(propsFile)
    return PropertiesConfigProxy.from(configInterface, props) as T
  }

  static class PropertiesConfigProxy extends DefaultConfiguration.ConfigurationInvocationHandler {
    def props

    static def from(Class configInterface, props) {
      return new PropertiesConfigProxy(props).around(configInterface, props)
    }

    PropertiesConfigProxy(props) {
      this.props = props
    }

    @Override
    public def around(Class configInterface, props) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new PropertiesConfigProxy(props))
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
      def node = props."${methodName}"
      if (node instanceof ConfigObject && node.isEmpty()) {
        throw new ConfigurationException(methodName, "not defined")
      }
      else {
        return node
      }
    }

  }
}
