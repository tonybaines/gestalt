package tonybaines.configuration

class XmlConfiguration<T> extends Configuration<T> {

  public XmlConfiguration(Class configInterface, String filePath) {
    super(configInterface, filePath)
  }

  public <T> T load() {
    def xml = new XmlParser().parse(source)
    return XmlConfigProxy.from(configInterface, xml) as T
  }

  static class XmlConfigProxy extends Configuration.ConfigurationInvocationHandler {
    def xml

    static def from(Class configInterface, xml) {
      return new XmlConfigProxy(xml).around(configInterface, xml)
    }

    XmlConfigProxy(xml) {
      this.xml = xml
    }

    @Override
    public def around(Class configInterface, xml) {
      java.lang.reflect.Proxy.newProxyInstance(Configuration.class.classLoader, (Class[]) [configInterface], new XmlConfigProxy(xml))
    }

    @Override
    protected String valueOf(node) {
      node.text()
    }

    @Override
    protected handleList(node, method) {
      return node.children().collect { child ->
        decoded(child, method.genericReturnType.actualTypeArguments[0])
      }
    }

    @Override
    protected lookUp(String methodName) {
      xml."${methodName}"[0]
    }

  }

}

