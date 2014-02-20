package tonybaines.configuration

class ConfigSlurperConfigProxy extends BaseConfiguration.ConfigurationInvocationHandler {
  def slurper

  ConfigSlurperConfigProxy(slurper) {
    this.slurper = slurper
  }

  @Override
  public def around(Class configInterface, slurper) {
    java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new ConfigSlurperConfigProxy(slurper))
  }

  @Override
  protected String valueOf(node) {
    node
  }

  @Override
  protected lookUp(String methodName) {
    def node = slurper."${methodName}"
    if (node instanceof ConfigObject && node.isEmpty()) {
      throw new ConfigurationException(methodName, "not defined")
    } else {
      return node
    }
  }

  @Override
  protected handleList(node, method) {
    return node.collect {
      decoded(it, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
