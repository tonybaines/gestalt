package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method


class CompositeConfiguration<T> implements Configuration<T> {
  private final List<Configuration<T>> configurations
  private final Class configInterface

  CompositeConfiguration(Class configInterface, List<Configuration<T>> configurations) {
    this.configInterface = configInterface
    this.configurations = configurations
  }

  @Override
  T load() {
    return CompositeConfigurationProxy.from(configInterface, configurations.collect { it.load() })
  }

  static class CompositeConfigurationProxy<T> implements InvocationHandler {
    private final List<T> configs

    static def from(Class configInterface, List<T> configs) {
      return new CompositeConfigurationProxy(configs).around(configInterface, configs)
    }

    CompositeConfigurationProxy(configs) {
      this.configs = configs
    }


    public def around(Class configInterface, configs) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new CompositeConfigurationProxy(configs))
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      tryAll(method, configs)
    }

    private Object tryAll(Method method, List<T> configs) {
      if (configs.empty) throw new ConfigurationException(method.name, "not found in any source")
      try {
        def config = configs.head()
        return config."${DefaultConfiguration.ConfigurationInvocationHandler.fromBeanSpec(method.name)}"
      }
      catch (Exception e) {
        return tryAll(method, configs.tail())
      }
    }
  }
}
