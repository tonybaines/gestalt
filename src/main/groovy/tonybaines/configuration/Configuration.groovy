package tonybaines.configuration

public interface Configuration<T> {

  T load()

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

    public CompositeConfigurationBuilder composedOf() {
      new CompositeConfigurationBuilder()
    }

    class CompositeConfigurationBuilder {
      List<Configuration<T>> strategies = new ArrayList<>()

      public Configuration.Factory.CompositeConfigurationBuilder thenFallbackToDefaults() {
        strategies << new Configuration<T>() {

          @Override
          T load() {
            return new DefaultConfiguration.ConfigurationInvocationHandler() {

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
        strategies << Configuration.Factory.this.fromXmlFile(filePath)
        this
      }

      public CompositeConfigurationBuilder fromPropertiesFile(String filePath) {
        strategies << Configuration.Factory.this.fromPropertiesFile(filePath)
        this
      }

      public CompositeConfigurationBuilder fromGroovyConfigFile(String filePath) {
        strategies << Configuration.Factory.this.fromGroovyConfigFile(filePath)
        this
      }

      public CompositeConfigurationBuilder first() { this }

      public CompositeConfigurationBuilder thenFallbackTo() { this }

      public <T> Configuration<T> done() {
        new CompositeConfiguration<T>(configInterface, strategies)
      }
    }

  }
}