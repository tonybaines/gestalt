package tonybaines.configuration

class CompositeConfiguration<T> extends Configuration<T> {
  CompositeConfiguration(Class configInterface, String filePath) {
    super(configInterface, filePath)
  }

  @Override
  def <T> T load() {
    return null
  }
}
