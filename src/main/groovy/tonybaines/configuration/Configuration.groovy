package tonybaines.configuration

import java.beans.Introspector
import java.lang.annotation.Annotation
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/*
 TODO: try building up paths e.g. ['config', 'strings'] and evaluating against the underlying store
 TODO: Filtering is the responsibility of client code, not config. 'id' is NOT a special case
  */

abstract class Configuration<T> {
  protected Class configInterface
  protected InputStream source

  public Configuration(Class configInterface, String filePath) {
    this.configInterface = configInterface
    source = Configuration.class.classLoader.getResourceAsStream(filePath)
  }

  static Factory<T> definedBy(Class configInterface) {
    new Factory(configInterface)
  }

  abstract <T> T load()

  static class Factory<T> {
    Class configInterface

    Factory(configInterface) {
      this.configInterface = configInterface
    }

    public <T> Configuration<T> fromXmlFile(String filePath) {
      new XmlConfiguration(configInterface, filePath)
    }

    public <T> Configuration<T> fromPropertiesFile(String filePath) {
      new PropertiesConfiguration(configInterface, filePath)
    }

    public <T> Configuration<T> fromGroovyConfigFile(String filePath) {
      new GroovyConfigConfiguration(configInterface, filePath)
    }

    public CompositeConfigurationBuilder composedOf(List<Configuration<T>> strategies) {
      new CompositeConfigurationBuilder()
    }

    class CompositeConfigurationBuilder {
      List<Configuration<T>> strategies = new ArrayList<>()

      public CompositeConfigurationBuilder thenFallbackToDefaults() {
        strategies << new Configuration<T>(null, null) {

          @Override
          def <T1> T1 load() {
            return new Configuration.ConfigurationInvocationHandler() {

              @Override
              protected String valueOf(Object x) {
                return null
              }

              @Override
              protected lookUp(String methodName) {
                return null
              }

              @Override
              protected handleList(Object node, Object method) {
                return null
              }

              @Override
              def around(Class configInterface, Object configSource) {
                return null
              }
            }
          }
        }
        this
      }

      public CompositeConfigurationBuilder fromXmlFile(String filePath) {
        strategies << Factory.this.fromXmlFile(filePath)
        this
      }

      public CompositeConfigurationBuilder first() { this }

      public CompositeConfigurationBuilder thenFallbackTo() { this }

      public CompositeConfigurationBuilder fromPropertiesFile(String filePath) {
        strategies << Factory.this.fromPropertiesFile(filePath)
        this
      }

      public CompositeConfigurationBuilder fromGroovyConfigFile(String filePath) {
        strategies << Factory.this.fromGroovyConfigFile(filePath)
        this
      }

      public <T> Configuration<T> done() {
        new CompositeConfiguration<T>(strategies)
      }
    }

  }

  static isAList(type) { type instanceof ParameterizedType && type.rawType.isAssignableFrom(List) }

  static abstract class ConfigurationInvocationHandler implements InvocationHandler {

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        def node = lookUp(fromBeanSpec(method.name))

        if (isAList(method.genericReturnType)) {
          return handleList(node, method)
        } else if (method.returnType.enum) {
          return method.returnType.valueOf(valueOf(node))
        } else {
          return decoded(node, method.returnType)
        }

      } catch (Throwable e) {
        Annotation defaultAnnotation = method.declaredAnnotations.find {
          it.annotationType().name.contains(Default.class.name)
        }
        if (defaultAnnotation != null) {
          return method.getAnnotation(defaultAnnotation.annotationType()).value()
        }
        throw new ConfigurationException(method, e)
      }
    }

    static String fromBeanSpec(String methodName) {
      Introspector.decapitalize(methodName.replace('get', ''))
    }

    protected def decoded(node, returnType) {
      switch (returnType) {
        case String: return valueOf(node)
        case Integer: return valueOf(node).toInteger()
        case Double: return valueOf(node).toDouble()
        case Boolean: return valueOf(node).toBoolean()

        default: return around(returnType, node)
      }
    }

    protected abstract String valueOf(x);

    protected abstract lookUp(String methodName);

    protected abstract handleList(node, method);

    public abstract def around(Class configInterface, configSource);
  }

}
