package tonybaines.configuration

class CompositeConfiguration<T> implements Configuration<T> {
  private final List<Configuration<T>> configurations

  CompositeConfiguration(List<Configuration<T>> configurations) {
    this.configurations = configurations
  }

  @Override
  T load() {
    return null
  }
}
