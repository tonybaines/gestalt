package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.serialisation.ConfigPropertiesSerialiser
import com.github.tonybaines.gestalt.serialisation.ConfigXmlSerialiser
import com.github.tonybaines.gestalt.sources.*
import com.github.tonybaines.gestalt.sources.features.CachingDecorator
import com.github.tonybaines.gestalt.sources.features.ExceptionOnNullValueDecorator
import com.github.tonybaines.gestalt.sources.features.ValidatingDecorator
import com.github.tonybaines.gestalt.transformers.DefaultPropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyNameTransformer
import com.github.tonybaines.gestalt.transformers.PropertyTypeTransformer
import com.github.tonybaines.gestalt.validation.ReflectionValidator
import com.github.tonybaines.gestalt.validation.ValidationResult
import groovy.transform.TupleConstructor
import groovy.util.logging.Slf4j

import javax.validation.constraints.AssertFalse
import javax.validation.constraints.AssertTrue
import javax.validation.constraints.DecimalMax
import javax.validation.constraints.DecimalMin
import javax.validation.constraints.Digits
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull
import javax.validation.constraints.Null
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size
import java.beans.Introspector
import java.lang.annotation.Annotation
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

import static com.github.tonybaines.gestalt.Configurations.Utils.propsFromString

@Slf4j
class Configurations<T> {
  static <T> CompositeConfigurationBuilder<T> definedBy(Class<T> configInterface) {
    new CompositeConfigurationBuilder<T>(configInterface)
  }

  static ValidationResult validate(Object instance, Class configInterface, PropertyTypeTransformer propertyTransformer = PropertyTypeTransformer.NULL) {
    new ReflectionValidator(instance, configInterface, propertyTransformer).validate()
  }

  static class Utils {
    static String fromBeanSpec(String methodName) {
      Introspector.decapitalize(methodName.replaceFirst(/(get|is)/, '')) // TODO: replace at start only
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
        case Long: return true
        case long: return true
        case Boolean: return true
        case boolean: return true

        default: return false
      }
    }

    static boolean declaresMethod(Class clazz, String name, Class... params) {
      clazz.methods.grep { !it.name.startsWith('\$') }.any {it.name == name && it.parameterTypes == params}
    }

    static Iterable declaredMethodsOf(Class configInterface) {
      configInterface.interface ? configInterface.methods.grep { !it.name.startsWith('\$') } : []
    }

    static boolean hasAFromStringMethod(Class clazz) {
      declaresMethod(clazz, 'fromString', String)
    }

    static boolean isDefaultReturningValidationResults(Method method) {
      method.isDefault() && method.returnType.equals(ValidationResult.class) || method.returnType.equals(ValidationResult.Item.class)
    }

    static boolean optional(Method method) {
      method.declaredAnnotations.any{it instanceof Optional}
    }

    static Properties propsFromString(String propsString) {
      Properties props = new Properties()
      propsString.eachLine {
        if (!it.startsWith('#')) {
          def (k,v) = it.split(/ = /)
          props.put(k, v)
        }
      }
      props
    }

    static def annotationInfo(Method method) {
      def info = []
      method.declaredAnnotations.each { Annotation a ->
        Class type = a.annotationType()

        if (type.name.contains(Comment.class.name)) {
          def comment = a.h.memberValues['value']
          info << "${comment}"
        }
        if (type.name.contains(Default.class.name)) {
          def defaultValue = a.h.memberValues['value']
          info << "default: ${defaultValue}"
        }

        if (type.canonicalName.startsWith('javax.validation.constraints')) {
          switch (type) {
            case Size: info << "[Size: min=${a.min()}, max=${a.max()}]"; break
            case AssertTrue: info << "[Always true]"; break
            case AssertFalse: info << "[Always false]"; break
            case DecimalMin: info << "[Decimal min=${a.value()}]"; break
            case DecimalMax: info << "[Decimal max=${a.value()}]"; break
            case Digits: info << "[Digits integer-digits=${a.integer()}, fraction-digits=${a.fraction()}]"; break
            case Min: info << "[Min ${a.value()}]"; break
            case Max: info << "[Max ${a.value()}]"; break
            case NotNull: info << "[Not Null]"; break
            case Null: info << "[Always Null]"; break
            case Pattern: info << "[Pattern ${a.regexp()}]"; break
          }
        }
      }
      info
    }

    static isNotAProperty(Object obj, String propertyName) {
      !obj.hasProperty(propertyName)
    }
  }

  static <T> SerialisationBuilder serialise(T instance, Class configInterface) {
    new SerialisationBuilder(instance, configInterface)
  }

  /**
   * @deprecated use {@see com.github.tonybaines.gestalt.Configurations#serialise(java.lang.Object, java.lang.Object)}
   */
  static <T> String toXml(T instance, Class configInterface, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()) {
    serialise(instance, configInterface).using(propertyNameTransformer).toXml()
  }

  /**
   * @deprecated use {@see com.github.tonybaines.gestalt.Configurations#serialise(java.lang.Object, java.lang.Object)}
   */
  static <T> Properties toProperties(T instance, Class configInterface, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()) {
    propsFromString(serialise(instance, configInterface).using(propertyNameTransformer).toProperties())
  }

  static class SerialisationBuilder<T> {
    private final T instance
    private final Class configInterface
    private PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()
    private PropertyTypeTransformer propertyTransformer = PropertyTypeTransformer.NULL
    boolean generatingComments = false

    SerialisationBuilder(T instance, Class configInterface) {
      this.configInterface = configInterface
      this.instance = instance
    }

    SerialisationBuilder using(PropertyNameTransformer propertyNameTransformer) {
      this.propertyNameTransformer = propertyNameTransformer
      this
    }

    SerialisationBuilder withPropertyTransformer(Class clazz) {
      this.propertyTransformer = PropertyTypeTransformer.from(clazz)
      this
    }

    SerialisationBuilder withComments() {
      this.generatingComments = true
      this
    }

    String toXml() {
      new ConfigXmlSerialiser(instance, propertyNameTransformer, propertyTransformer, generatingComments).toXmlString(configInterface)
    }

    String toProperties() {
      new ConfigPropertiesSerialiser(instance, propertyNameTransformer, propertyTransformer, generatingComments).toProperties(configInterface)
    }
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
    private PropertyTypeTransformer propertyTransformer = PropertyTypeTransformer.NULL


    @TupleConstructor
    private static final class Source {
      enum SourceType {
        XMLStream, GroovyStream, PropertiesStream, Properties, ConfigSource, ConfigInstance
      }

      final SourceType type
      final def source
      final PropertyNameTransformer propNameTxformer
    }

    CompositeConfigurationBuilder(Class<T> configInterface) {
      this.configInterface = configInterface
    }

    private List<Feature> enabledFeatures = Feature.values().clone()

    private def sources = new ArrayList<>()

    CompositeConfigurationBuilder<T> fromXmlResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromXml(resourceAsStream(filePath, clazz))
      }
      this
    }

    CompositeConfigurationBuilder<T> fromXmlFile(String filePath, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer(), Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromXml(new File(filePath).newInputStream())
      }
      this
    }

    CompositeConfigurationBuilder<T> fromPropertiesResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromProperties(resourceAsStream(filePath, clazz))
      }
      this
    }

    CompositeConfigurationBuilder<T> fromPropertiesFile(String filePath, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer(), Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromProperties(new File(filePath).newInputStream())
      }
      this
    }

    CompositeConfigurationBuilder<T> fromGroovyConfigResource(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromGroovyConfig(resourceAsStream(filePath, clazz))
      }
      this
    }

    CompositeConfigurationBuilder<T> fromGroovyConfigFile(String filePath, Class clazz = null, Behaviour... behaviours) {
      tryToLoadWith(behaviours, filePath) {
        fromGroovyConfig(new File(filePath).newInputStream())
      }
      this
    }

    CompositeConfigurationBuilder<T> withConstants(Map<String, String> constants) {
      this.constants = constants
      this
    }

    CompositeConfigurationBuilder<T> withConstants(Properties props) {
      withConstants(props.asImmutable() as Map<String, String>)
    }

    CompositeConfigurationBuilder<T> withConstantsFromResource(String filePath, Class clazz = null) {
      Properties props = new Properties()
      props.load(resourceAsStream(filePath, clazz))
      withConstants(props)
    }

    CompositeConfigurationBuilder<T> withPropertyTransformer(Class clazz) {
      this.propertyTransformer = PropertyTypeTransformer.from(clazz)
      this
    }


    private def tryToLoadWith(behaviours, filePath, Closure c) {
      try {
        c.call(filePath)
      }
      catch (Throwable e) {
        if (isOptional(behaviours)) log.warn("Could not load an optional configuration: ${e.message}", e)
        else {
          def ex = new ConfigurationException("Could not load from a required configuration source: ${e.message}")
          ex.printStackTrace()
          throw ex
        }
      }
    }

    CompositeConfigurationBuilder<T> from(ConfigSource configSource) {
      streams << new Source(Source.SourceType.ConfigSource, configSource, null)
      this
    }

    CompositeConfigurationBuilder<T> fromXml(InputStream stream, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()) {
      streams << new Source(Source.SourceType.XMLStream, stream, propertyNameTransformer)
      this
    }

    CompositeConfigurationBuilder<T> fromProperties(InputStream stream, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()) {
      streams << new Source(Source.SourceType.PropertiesStream, stream, propertyNameTransformer)
      this
    }

    CompositeConfigurationBuilder<T> fromProperties(Properties props, PropertyNameTransformer propertyNameTransformer = new DefaultPropertyNameTransformer()) {
      streams << new Source(Source.SourceType.Properties, props, propertyNameTransformer)
      this
    }

    CompositeConfigurationBuilder<T> fromGroovyConfig(InputStream stream) {
      streams << new Source(Source.SourceType.GroovyStream, stream, new DefaultPropertyNameTransformer())
      this
    }

    CompositeConfigurationBuilder<T> fromConfigInstance(T configInstance) {
      streams << new Source(Source.SourceType.ConfigInstance, configInstance, null)
      this
    }

    CompositeConfigurationBuilder<T> without(Feature... feature) {
      feature.each { enabledFeatures.remove(it) }
      this
    }

    T done() {
      loadAllSources()

      if (sources.isEmpty()) tryToLoadDefaultSource(configInterface)
      if (enabledFeatures.contains(Feature.Defaults)) sources << new DefaultConfigSource(propertyTransformer)

      new DynoClass<T>(
        withExceptionOnNullValue(withCaching(new CompositeConfigSource(sources
          .collect { withValidation(it) }
        ))), propertyTransformer).getMapAsInterface(configInterface)
    }

    private tryToLoadDefaultSource(Class clazz) {
      // Fall-back to loading a file <class-name>.properties from the current directory
      def fallbackSource = new File("${clazz.simpleName}.properties")
      log.warn("No valid sources configured.  Falling back to: ${fallbackSource.absolutePath}")
      try {
        def fallbackSourceStream = fallbackSource.newInputStream()
        sources << new PropertiesConfigSource(fallbackSourceStream, new DefaultPropertyNameTransformer(), PropertyTypeTransformer.NULL, constants)
      } catch (Exception ignored) {
        throw new ConfigurationException("No valid sources available!")
      }
    }

    private loadAllSources() {
      streams.each { source ->
        switch (source.type) {
          case Source.SourceType.ConfigInstance: sources << new InstanceConfigSource(source.source); break
          case Source.SourceType.ConfigSource: sources << source.source; break
          case Source.SourceType.XMLStream: sources << new XmlConfigSource(source.source, source.propNameTxformer, propertyTransformer, constants); break
          case Source.SourceType.PropertiesStream: sources << new PropertiesConfigSource(source.source, source.propNameTxformer, propertyTransformer, constants); break
          case Source.SourceType.Properties: sources << new PropertiesConfigSource(source.source, source.propNameTxformer, propertyTransformer, constants); break
          case Source.SourceType.GroovyStream: sources << new GroovyConfigSource(source.source, source.propNameTxformer, propertyTransformer, constants); break
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

    private static boolean isOptional(Behaviour... behaviours) {
      behaviours.contains(Behaviour.isOptional)
    }

    private static InputStream resourceAsStream(String path, Class loadingClass) {
      def loader = loadingClass != null ? loadingClass : Configurations.class.classLoader
      def resourceStream = loader.getResourceAsStream(path)
      if (resourceStream == null) throw new ConfigurationException("Could not load the configuration resource from '$path'")
      resourceStream
    }

  }

}