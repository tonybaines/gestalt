package tonybaines.configuration

import tonybaines.configuration.sources.*
import tonybaines.configuration.sources.features.ExceptionOnNullValueDecorator
import tonybaines.configuration.sources.features.ValidatingDecorator

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

  static enum Feature {
    Validation, Defaults, ExceptionOnNullValue, Caching
  }

  static interface Configuration<T> {

    static class Factory<T> {
      Class configInterface

      Factory(configInterface) {
        this.configInterface = configInterface
      }

      public T fromXmlFile(String filePath) {
        new DynoClass<T>(new XmlConfigSource(filePath)).getMapAsInterface(configInterface)
      }

      public T fromPropertiesFile(String filePath) {
        new DynoClass<T>(new PropertiesConfigSource(filePath)).getMapAsInterface(configInterface)
      }

      public T fromGroovyConfigFile(String filePath) {
        new DynoClass<T>(new GroovyConfigSource(filePath)).getMapAsInterface(configInterface)
      }

      public CompositeConfigurationBuilder<T> composedOf() {
        new CompositeConfigurationBuilder()
      }

      class CompositeConfigurationBuilder<T> {
        List<Feature> enabledFeatures = Feature.values().clone()

        def sources = new ArrayList<>()

        public CompositeConfigurationBuilder<T> fromXmlFile(String filePath) {
          sources << new XmlConfigSource(filePath)
          this
        }

        public CompositeConfigurationBuilder<T> fromPropertiesFile(String filePath) {
          sources << new PropertiesConfigSource(filePath)
          this
        }

        public CompositeConfigurationBuilder<T> fromGroovyConfigFile(String filePath) {
          sources << new GroovyConfigSource(filePath)
          this
        }

        def without(Feature... feature) {
          feature.each { enabledFeatures.remove(it) }
          this
        }

        public T done() {
          if (enabledFeatures.contains(Feature.Defaults)) sources << new DefaultConfigSource()

          new DynoClass<T>(
            withExceptionOnNullValue(new CompositeConfigSource(sources
              .collect { withValidation(it) }
            ))).getMapAsInterface(configInterface)
        }

        ConfigSource withValidation(ConfigSource source) {
          if (enabledFeatures.contains(Feature.Validation)) new ValidatingDecorator<>(source)
          else source
        }

        ConfigSource withExceptionOnNullValue(ConfigSource source) {
          if (enabledFeatures.contains(Feature.ExceptionOnNullValue)) new ExceptionOnNullValueDecorator<>(source)
          else source
        }
      }


    }
  }
}
