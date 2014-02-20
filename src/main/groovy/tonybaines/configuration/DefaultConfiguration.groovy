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
      try {
        Annotation defaultAnnotation = method.declaredAnnotations.find {
          it.annotationType().name.contains(Default.class.name)
        }
        if (defaultAnnotation != null) {
          Class<? extends Annotation> defaultsType = defaultAnnotation.annotationType()
          def annotationValue = method.getAnnotation(defaultsType).value()

          if (Default.Enum.class.name.equals(defaultsType.name)) {
            return method.returnType.valueOf(annotationValue)
          } else {
            return annotationValue
          }
        }
      } catch (Exception e) {
        throw new ConfigurationException(method, e)
      }
      throw new ConfigurationException(method.name, "no default value defined")
    }

    @Override
    protected String valueOf(node) { throw new UnsupportedOperationException() }

    @Override
    protected lookUp(String methodName) { throw new UnsupportedOperationException() }

    @Override
    protected handleList(node, method) { throw new UnsupportedOperationException() }
  }
}
