package alltehcode.gestalt

import java.lang.reflect.Method

public interface ConfigSource {

  def lookup(List<String> path, Method method)

}