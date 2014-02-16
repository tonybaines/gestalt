package tonybaines.configuration
/*
 TODO: try building up paths e.g. ['config', 'strings'] and evaluating against the underlying store
 TODO: Filtering is the responsibility of client code, not config. 'id' is NOT a special case
  */

abstract class Configurations<T> {

  static Factory<T> definedBy(Class configInterface) {
    new Factory(configInterface)
  }

  abstract <T> T load()

  static class Factory<T> {
    Class configInterface

    Factory(configInterface) {
      this.configInterface = configInterface
    }

    public <T> Configurations<T> fromXmlFile(String filePath) {
      new XmlConfigurations(configInterface, filePath)
    }

    public <T> Configurations<T> fromPropertiesFile(String filePath) {
      new PropertiesConfigurations(configInterface, filePath)
    }
  }

}
