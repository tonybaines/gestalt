package tonybaines.configuration

class GroovyConfigConfiguration<T> extends DefaultConfiguration<T> {
  String filePath

  public GroovyConfigConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    def groovyConfig = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
    return GroovyConfigConfigProxy.from(configInterface, groovyConfig) as T
  }

  static class GroovyConfigConfigProxy extends DefaultConfiguration.ConfigurationInvocationHandler {
    def groovyConfig

    static def from(Class configInterface, props) {
      return new GroovyConfigConfigProxy(props).around(configInterface, props)
    }

    GroovyConfigConfigProxy(groovyConfig) {
      this.groovyConfig = groovyConfig
    }

    @Override
    public def around(Class configInterface, groovyConfig) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new GroovyConfigConfigProxy(groovyConfig))
    }

    @Override
    protected String valueOf(node) { node }

    @Override
    protected handleList(node, method) {
      node.collect {
        decoded(it, method.genericReturnType.actualTypeArguments[0])
      }
    }

    @Override
    protected lookUp(String methodName) {
      def node = groovyConfig."${methodName}"
      if (node instanceof ConfigObject && node.isEmpty()) {
        throw new ConfigurationException(methodName, "not defined")
      }
      else {
        return node
      }

    }

  }
}
