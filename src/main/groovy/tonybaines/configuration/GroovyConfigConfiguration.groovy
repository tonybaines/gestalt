package tonybaines.configuration

class GroovyConfigConfiguration<T> extends Configuration<T> {

  public GroovyConfigConfiguration(Class configInterface, String filePath) {
    super(configInterface, filePath)
  }

  public <T> T load() {
    def groovyConfig = new ConfigSlurper().parse(source.text).values().first()
    return GroovyConfigConfigProxy.from(configInterface, groovyConfig) as T
  }

  static class GroovyConfigConfigProxy extends Configuration.ConfigurationInvocationHandler {
    def groovyConfig

    static def from(Class configInterface, props) {
      return new GroovyConfigConfigProxy(props).around(configInterface, props)
    }

    GroovyConfigConfigProxy(groovyConfig) {
      this.groovyConfig = groovyConfig
    }

    @Override
    public def around(Class configInterface, groovyConfig) {
      java.lang.reflect.Proxy.newProxyInstance(Configuration.class.classLoader, (Class[]) [configInterface], new GroovyConfigConfigProxy(groovyConfig))
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
