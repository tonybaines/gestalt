package tonybaines.configuration

import javax.validation.Validation
import javax.validation.Validator
import java.beans.Introspector

class Configurations<T> {
  static <T> Configuration.Factory<T> definedBy(Class configInterface) {
    new Configuration.Factory(configInterface)
  }

  static String fromBeanSpec(String methodName) {
    Introspector.decapitalize(methodName.replace('get', ''))
  }

  static void validate(T config) {
    Validator validator = Validation.buildDefaultValidatorFactory().validator
    def validationResults = validator.validate(config)
    if (!validationResults.empty) {
      throw new ConfigurationException(validationResults.collect { "${it.propertyPath} ${it.interpolatedMessage}" })
    }
  }

  public static interface Configuration<T> {

    T load()

    static class Factory<T> {
      Class configInterface
      boolean validateOnLoad

      Factory(configInterface) {
        this.configInterface = configInterface
      }

      public <T> Factory<T> validateOnLoad() {
        validateOnLoad = true
        this
      }

      public <T> Configuration<T> fromXmlFile(String filePath, boolean shouldValidateIfAsked = true) {
        new XmlConfiguration(configInterface, filePath, validateOnLoad && shouldValidateIfAsked)
      }

      public <T> Configuration<T> fromPropertiesFile(String filePath, boolean shouldValidateIfAsked = true) {
        new PropertiesConfiguration(configInterface, filePath, validateOnLoad && shouldValidateIfAsked)
      }

      public <T> Configuration<T> fromGroovyConfigFile(String filePath, boolean shouldValidateIfAsked = true) {
        new GroovyConfigConfiguration(configInterface, filePath, validateOnLoad && shouldValidateIfAsked)
      }

      public <T> Configuration<T> fromDefaults() {
        new DefaultConfiguration(configInterface)
      }

      public CompositeConfigurationBuilder<T> composedOf() {
        new CompositeConfigurationBuilder()
      }

      class CompositeConfigurationBuilder<T> {
        List<Configuration<T>> strategies = new ArrayList<>()

        public CompositeConfigurationBuilder<T> thenFallbackToDefaults() {
          strategies << Factory.this.fromDefaults()
          this
        }

        public CompositeConfigurationBuilder<T> fromXmlFile(String filePath) {
          strategies << Factory.this.fromXmlFile(filePath, !validateOnLoad)
          this
        }

        public CompositeConfigurationBuilder<T> fromPropertiesFile(String filePath) {
          strategies << Factory.this.fromPropertiesFile(filePath, !validateOnLoad)
          this
        }

        public CompositeConfigurationBuilder<T> fromGroovyConfigFile(String filePath) {
          strategies << Factory.this.fromGroovyConfigFile(filePath, !validateOnLoad)
          this
        }

        public <T> Configuration<T> done() {
          new CompositeConfiguration<T>(configInterface, strategies, validateOnLoad)
        }
      }

    }
  }
}
