package tonybaines.configuration

import java.lang.annotation.Annotation
import java.lang.reflect.Method

class DefaultConfiguration<T> extends BaseConfiguration<T> {

  DefaultConfiguration(Class configInterface) {
    super(configInterface)
  }

  @Override
  T load() {
    return new DefaultConfigProxy().around(configInterface) as T
  }

  static class DefaultConfigProxy extends BaseConfiguration.ConfigurationInvocationHandler {


    @Override
    def around(Class configInterface, Object configSource = null) {
      java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], this)
    }

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      Annotation defaultAnnotation = method.declaredAnnotations.find {
        it.annotationType().name.contains(Default.class.name)
      }
      if (defaultAnnotation != null) {
        return method.getAnnotation(defaultAnnotation.annotationType()).value()
      }
      throw new ConfigurationException(method.name, "no default value defined")
    }

    @Override
    protected String valueOf(node) {
      throw new UnsupportedOperationException()
    }

    @Override
    protected lookUp(String methodName) {
      throw new UnsupportedOperationException()
    }

    @Override
    protected handleList(node, method) {
      throw new UnsupportedOperationException()
    }
  }
}
