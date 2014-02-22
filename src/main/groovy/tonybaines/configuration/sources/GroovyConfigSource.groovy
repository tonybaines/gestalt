package tonybaines.configuration.sources

import groovy.util.logging.Slf4j
import tonybaines.configuration.ConfigSource
import tonybaines.configuration.ConfigurationException
import tonybaines.configuration.Configurations
import tonybaines.configuration.DynoClass

import java.lang.reflect.Method

@Slf4j
class GroovyConfigSource implements ConfigSource {
  def config

  GroovyConfigSource(String filePath) {
    config = new ConfigSlurper().parse(this.class.classLoader.getResourceAsStream(filePath).text).values().first()
  }

  private GroovyConfigSource(Object node) {
    config = node
  }

  @Override
  def lookup(List<String> path, Method method) {
    try {
      def node = path.inject(config) { acc, val -> acc."$val" }
      if (method.returnType.enum) {
        def stringValue = valueOf(node)
        return (stringValue != null) ? method.returnType.valueOf(stringValue) : null
      }
      if (Configurations.isAList(method.genericReturnType)) {
        def list = handleList(node, method)
        return list
      }

      return decoded(node, method.returnType)
    } catch (Throwable e) {
      log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigProxy', '')}"
      throw new ConfigurationException(method, e)
    }
  }

  protected def decoded(node, Class returnType) {
    switch (returnType) {
      case String: return valueOf(node)
      case Integer: return valueOf(node)?.toInteger()
      case Double: return valueOf(node)?.toDouble()
      case Boolean: return valueOf(node)?.toBoolean()

      default: new DynoClass(new GroovyConfigSource(node)).getMapAsInterface(returnType)
    }
  }

  protected String valueOf(node) {
    if (node instanceof ConfigObject && node.isEmpty()) return null
    else node
  }

  protected handleList(node, method) {
    node.collect {
      decoded(it, method.genericReturnType.actualTypeArguments[0])
    }
  }
}
