package tonybaines.configuration

public interface Configuration<T> {

  T load()

  static class Factory<T> {
    Class configInterface

    Factory(configInterface) {
      this.configInterface = configInterface
    }

    public <T> DefaultConfiguration<T> fromXmlFile(String filePath) {
      new XmlConfiguration(configInterface, filePath)
    }

    public <T> DefaultConfiguration<T> fromPropertiesFile(String filePath) {
      new PropertiesConfiguration(configInterface, filePath)
    }

    public <T> DefaultConfiguration<T> fromGroovyConfigFile(String filePath) {
      new GroovyConfigConfiguration(configInterface, filePath)
    }

    public Configuration.Factory.CompositeConfigurationBuilder composedOf(List<DefaultConfiguration<T>> strategies) {
      new Configuration.Factory.CompositeConfigurationBuilder()
    }

    class CompositeConfigurationBuilder {
      List<DefaultConfiguration<T>> strategies = new ArrayList<>()

      public Configuration.Factory.CompositeConfigurationBuilder thenFallbackToDefaults() {
        strategies << new DefaultConfiguration<T>(null) {

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

      public Configuration.Factory.CompositeConfigurationBuilder fromXmlFile(String filePath) {
        strategies << Configuration.Factory.this.fromXmlFile(filePath)
        this
      }

      public Configuration.Factory.CompositeConfigurationBuilder first() { this }

      public Configuration.Factory.CompositeConfigurationBuilder thenFallbackTo() { this }

      public Configuration.Factory.CompositeConfigurationBuilder fromPropertiesFile(String filePath) {
        strategies << Configuration.Factory.this.fromPropertiesFile(filePath)
        this
      }

      public Configuration.Factory.CompositeConfigurationBuilder fromGroovyConfigFile(String filePath) {
        strategies << Configuration.Factory.this.fromGroovyConfigFile(filePath)
        this
      }

      public <T> DefaultConfiguration<T> done() {
        new CompositeConfiguration<T>(strategies)
      }
    }

  }
}