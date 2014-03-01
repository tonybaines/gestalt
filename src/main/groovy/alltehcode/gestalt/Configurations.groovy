package alltehcode.gestalt

import alltehcode.gestalt.sources.*
import alltehcode.gestalt.sources.features.CachingDecorator
import alltehcode.gestalt.sources.features.ExceptionOnNullValueDecorator
import alltehcode.gestalt.sources.features.ValidatingDecorator

import java.beans.Introspector
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

class Configurations<T> {
  static <T> CompositeConfigurationBuilder<T> definedBy(Class<T> configInterface) {
    new CompositeConfigurationBuilder<T>(configInterface)
  }


  static class Utils {
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
  }

  static def toXml(instance, Class configInterface) {
    new ConfigXmlSerialiser(instance).toXmlString(configInterface)
  }

  static enum Feature {
    Validation, Defaults, ExceptionOnNullValue, Caching
  }


  static class CompositeConfigurationBuilder<T> {
    private Class configInterface

    CompositeConfigurationBuilder(Class<T> configInterface) {
      this.configInterface = configInterface
    }

    private List<Feature> enabledFeatures = Feature.values().clone()

    private def sources = new ArrayList<>()

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

    private ConfigSource withValidation(ConfigSource source) {
      if (enabledFeatures.contains(Feature.Validation)) new ValidatingDecorator<>(source)
      else source
    }

    private ConfigSource withCaching(ConfigSource source) {
      if (enabledFeatures.contains(Feature.Caching)) new CachingDecorator<>(source)
      else source
    }

    private ConfigSource withExceptionOnNullValue(ConfigSource source) {
      if (enabledFeatures.contains(Feature.ExceptionOnNullValue)) new ExceptionOnNullValueDecorator<>(source)
      else source
    }

    private static InputStream resourceAsStream(String path) {
      Configurations.class.classLoader.getResourceAsStream(path)
    }

  }

}
