package tonybaines.configuration

import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

@Slf4j
class CompositeConfiguration<T> implements Configurations.Configuration<T> {
  private final List<Configurations.Configuration<T>> configurations
  private final Class configInterface
  private final boolean validateOnLoad

  CompositeConfiguration(Class configInterface, List<Configurations.Configuration<T>> configurations, boolean validateOnLoad) {
    this.configInterface = configInterface
    this.configurations = configurations
    this.validateOnLoad = validateOnLoad
  }

  @Override
  T load() {
    def config = new CompositeConfigurationProxy(configurations.collect { it.load() }).around(configInterface) as T
    if (validateOnLoad) Configurations.validate(config)
    config
  }

  static class CompositeConfigurationProxy<T> implements InvocationHandler {
    private final List<T> configs

    CompositeConfigurationProxy(configs) {
      this.configs = configs
    }

    @Override
    public def around(Class configInterface) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], this)
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      tryAll(method, configs)
    }

    private Object tryAll(Method method, List<T> configs) {
      if (method.name.equals("hashCode")) return 1
      if (method.name.equals("equals")) return false

      if (configs.empty) throw new ConfigurationException(method.name, "not found in any source")
      def config = configs.head()
      try {
        return config."${Configurations.fromBeanSpec(method.name)}"
      }
      catch (Exception e) {
        if (configs.tail().empty) throw e
        else {
          log.info "No definition found for ${method.name}. Falling back"
          return tryAll(method, configs.tail())
        }
      }
    }
  }
}
