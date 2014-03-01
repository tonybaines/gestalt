package alltehcode.gestalt

import alltehcode.gestalt.sources.*
import alltehcode.gestalt.sources.features.CachingDecorator
import alltehcode.gestalt.sources.features.ExceptionOnNullValueDecorator
import alltehcode.gestalt.sources.features.ValidatingDecorator

import java.beans.Introspector
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class Configurations<T> {
  static <T> Configuration.Factory<T> definedBy(Class<T> configInterface) {
    new Configuration<T>.Factory<T>(configInterface)
  }

  static String fromBeanSpec(String methodName) {
    Introspector.decapitalize(methodName.replaceFirst(/(get|is)/, ''))
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

  static def toXml(instance, Class configInterface) {
    new ConfigXmlSerialiser(instance).toXmlString(configInterface)
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
        new DynoClass<T>(new XmlConfigSource(resourceAsStream(filePath))).getMapAsInterface(configInterface)
      }

      public T fromPropertiesFile(String filePath) {
        new DynoClass<T>(new PropertiesConfigSource(resourceAsStream(filePath))).getMapAsInterface(configInterface)
      }

      public T fromGroovyConfigFile(String filePath) {
        new DynoClass<T>(new GroovyConfigSource(resourceAsStream(filePath))).getMapAsInterface(configInterface)
      }

      public CompositeConfigurationBuilder<T> composedOf() {
        new CompositeConfigurationBuilder()
      }

      private static InputStream resourceAsStream(String path) {
        Configurations.class.classLoader.getResourceAsStream(path)
      }

      class CompositeConfigurationBuilder<T> {
        List<Feature> enabledFeatures = Feature.values().clone()

        def sources = new ArrayList<>()

        public CompositeConfigurationBuilder<T> fromXmlFile(String filePath) {
          sources << new XmlConfigSource(resourceAsStream(filePath))
          this
        }

        public CompositeConfigurationBuilder<T> fromPropertiesFile(String filePath) {
          sources << new PropertiesConfigSource(resourceAsStream(filePath))
          this
        }

        public CompositeConfigurationBuilder<T> fromGroovyConfigFile(String filePath) {
          sources << new GroovyConfigSource(resourceAsStream(filePath))
          this
        }

        def without(Feature... feature) {
          feature.each { enabledFeatures.remove(it) }
          this
        }

        public T done() {
          if (enabledFeatures.contains(Feature.Defaults)) sources << new DefaultConfigSource()

          new DynoClass<T>(
            withExceptionOnNullValue(withCaching(new CompositeConfigSource(sources
              .collect { withValidation(it) }
            )))).getMapAsInterface(configInterface)
        }

        ConfigSource withValidation(ConfigSource source) {
          if (enabledFeatures.contains(Feature.Validation)) new ValidatingDecorator<>(source)
          else source
        }

        ConfigSource withCaching(ConfigSource source) {
          if (enabledFeatures.contains(Feature.Caching)) new CachingDecorator<>(source)
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
