package tonybaines.configuration

public interface ConfigSource {

  def lookup(List<String> path)

}