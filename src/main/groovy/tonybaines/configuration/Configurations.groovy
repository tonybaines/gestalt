package tonybaines.configuration

import tonybaines.configuration.sources.*

import java.beans.Introspector
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class Configurations<T> {
  static <T> Configuration.Factory<T> definedBy(Class configInterface) {
    new Configuration.Factory(configInterface)
  }

  static String fromBeanSpec(String methodName) {
    Introspector.decapitalize(methodName.replace('get', ''))
  }

  static isAList(type) {
    type instanceof ParameterizedType && type.rawType.isAssignableFrom(List)
  }

  static boolean returnsAValue(Method method) {
    isAValueType(method.returnType)
  }

  static isAValueType(Class type) {
    switch (type) {
      case String: return true
      case Integer: return true
      case Double: return true
      case Boolean: return true

      default: return false
    }
  }

  public static interface Configuration<T> {

    static class Factory<T> {
      Class configInterface

      Factory(configInterface) {
        this.configInterface = configInterface
      }

      public T fromXmlFile(String filePath) {
        new DynoClass<T>(new ValidatingDecorator<T>(configInterface, new XmlConfigSource(filePath))).getMapAsInterface(configInterface)
      }

      public T fromPropertiesFile(String filePath) {
        new DynoClass<T>(new ValidatingDecorator<T>(configInterface, new PropertiesConfigSource(filePath))).getMapAsInterface(configInterface)
      }

      public T fromGroovyConfigFile(String filePath) {
        new DynoClass<T>(new ValidatingDecorator<T>(configInterface, new GroovyConfigSource(filePath))).getMapAsInterface(configInterface)
      }

      public CompositeConfigurationBuilder<T> composedOf() {
        new CompositeConfigurationBuilder()
      }

      class CompositeConfigurationBuilder<T> {
        List<T> sources = new ArrayList<>()

        public CompositeConfigurationBuilder<T> thenFallbackToDefaults() {
          sources << new ValidatingDecorator<T>(configInterface, new DefaultConfigSource())
          this
        }

        public CompositeConfigurationBuilder<T> fromXmlFile(String filePath) {
          sources << new ValidatingDecorator<T>(configInterface, new XmlConfigSource(filePath))
          this
        }

        public CompositeConfigurationBuilder<T> fromPropertiesFile(String filePath) {
          sources << new ValidatingDecorator<T>(configInterface, new PropertiesConfigSource(filePath))
          this
        }

        public CompositeConfigurationBuilder<T> fromGroovyConfigFile(String filePath) {
          sources << new ValidatingDecorator<T>(configInterface, new GroovyConfigSource(filePath))
          this
        }

        public T done() {
          new DynoClass<T>(new CompositeConfigSource(sources)).getMapAsInterface(configInterface)
        }
      }

    }
  }
}
