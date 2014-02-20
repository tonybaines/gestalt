package tonybaines.configuration

import groovy.util.logging.Slf4j

@Slf4j
class XmlConfiguration<T> extends BaseConfiguration<T> {
  String filePath

  public XmlConfiguration(Class configInterface, String filePath) {
    super(configInterface)
    this.filePath = filePath
  }

  public T load() {
    log.info "Loading XML configuration from $filePath"
    def xml = new XmlParser().parse(this.class.classLoader.getResourceAsStream(filePath))
    return new XmlConfigProxy(xml).around(configInterface, xml) as T
  }

  static class XmlConfigProxy extends BaseConfiguration.ConfigurationInvocationHandler {
    def xml

    XmlConfigProxy(xml) {
      this.xml = xml
    }

    @Override
    public def around(Class configInterface, xml) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new XmlConfigProxy(xml))
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
      def matches = xml."${methodName}"
      if (matches.size() > 1) throw new ConfigurationException(methodName, "more than one definition")
      matches[0]
    }

  }

}

