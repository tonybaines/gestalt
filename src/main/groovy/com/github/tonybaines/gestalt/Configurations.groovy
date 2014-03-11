package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.sources.*
import com.github.tonybaines.gestalt.sources.features.CachingDecorator
import com.github.tonybaines.gestalt.sources.features.ExceptionOnNullValueDecorator
import com.github.tonybaines.gestalt.sources.features.ValidatingDecorator
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

import java.beans.Introspector
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Slf4j
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
        case int: return true
        case Double: return true
        case double: return true
        case Boolean: return true
        case boolean: return true

        default: return false
      }
    }
  }

  static String toXml(instance, Class configInterface) {
    new ConfigXmlSerialiser(instance).toXmlString(configInterface)
  }

  static <T> Properties toProperties(instance, T configInterface) {
    new ConfigPropertiesSerialiser(instance).toProperties(configInterface)
  }

  static enum Feature {
    Validation, Defaults, ExceptionOnNullValue, Caching
  }

  static enum Behaviour {
    isOptional
  }

  static class CompositeConfigurationBuilder<T> {
    private Class configInterface
    private Map<String, String> constants = [:]
    private List<Source> streams = []


    @TupleConstructor
    private static final class Source {
      public enum SourceType {
        XMLStream, GroovyStream, PropertiesStream, Properties
      }

      final SourceType type
      final def source
    }

    CompositeConfigurationBuilder(Class<T> configInterface) {
      this.configInterface = configInterface
    }

    private List<Feature> enabledFeatures = Feature.values().clone()

    private def sources = new ArrayList<>()

    public CompositeConfigurationBuilder<T> fromXmlResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromXml(resourceAsStream(filePath, clazz))
      }
      this
    }

    public CompositeConfigurationBuilder<T> fromPropertiesResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromProperties(resourceAsStream(filePath, clazz))
      }
      this
    }

    public CompositeConfigurationBuilder<T> fromGroovyConfigResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromGroovyConfig(resourceAsStream(filePath, clazz))
      }
      this
    }

    public CompositeConfigurationBuilder<T> withConstants(Map<String, String> constants) {
      this.constants = constants
      this
    }

    public CompositeConfigurationBuilder<T> withConstants(Properties props) {
      withConstants(props.asImmutable() as Map<String, String>)
    }

    public CompositeConfigurationBuilder<T> withConstantsFromResource(String filePath, Class clazz = null) {
      Properties props = new Properties()
      props.load(resourceAsStream(filePath, clazz))
      withConstants(props)
    }


    private def tryToLoadWith(behaviours, filePath, Closure c) {
      try {
        c.call(filePath)
      }
      catch (Throwable e) {
        if (isOptional(behaviours)) log.warn("Could not load the configuration from '$filePath', but it is optional so continuing", e)
        else throw new ConfigurationException('Could not load the configuration', e)
      }
    }

    public CompositeConfigurationBuilder<T> fromXml(InputStream stream) {
      streams << new Source(Source.SourceType.XMLStream, stream)
      this
    }

    public CompositeConfigurationBuilder<T> fromProperties(InputStream stream) {
      streams << new Source(Source.SourceType.PropertiesStream, stream)
      this
    }

    public CompositeConfigurationBuilder<T> fromProperties(Properties props) {
      streams << new Source(Source.SourceType.Properties, props)
      this
    }

    public CompositeConfigurationBuilder<T> fromGroovyConfig(InputStream stream) {
      streams << new Source(Source.SourceType.GroovyStream, stream)
      this
    }

    public CompositeConfigurationBuilder<T> without(Feature... feature) {
      feature.each { enabledFeatures.remove(it) }
      this
    }

    public T done() {
      loadAllSources()

      if (sources.isEmpty()) throw new ConfigurationException("No valid sources configured!")
      if (enabledFeatures.contains(Feature.Defaults)) sources << new DefaultConfigSource()

      new DynoClass<T>(
        withExceptionOnNullValue(withCaching(new CompositeConfigSource(sources
          .collect { withValidation(it) }
        )))).getMapAsInterface(configInterface)
    }

    private loadAllSources() {
      streams.each { source ->
        switch (source.type) {
          case Source.SourceType.XMLStream: sources << new XmlConfigSource(source.source, constants); break
          case Source.SourceType.PropertiesStream: sources << new PropertiesConfigSource(source.source, constants); break
          case Source.SourceType.Properties: sources << new PropertiesConfigSource(source.source, constants); break
          case Source.SourceType.GroovyStream: sources << new GroovyConfigSource(source.source, constants); break
        }
      }
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

    private boolean isOptional(Configurations.Behaviour... behaviours) {
      behaviours.contains(Configurations.Behaviour.isOptional)
    }

    private static InputStream resourceAsStream(String path, Class loadingClass) {
      def loader = loadingClass != null ? loadingClass : Configurations.class.classLoader
      def resourceStream = loader.getResourceAsStream(path)
      if (resourceStream == null) throw new ConfigurationException("Could not load the configuration from '$path'")
      resourceStream
    }

  }

}