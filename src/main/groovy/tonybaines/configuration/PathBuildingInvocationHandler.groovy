package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class PathBuildingInvocationHandler implements InvocationHandler {
  private def source
  private final Object path

  PathBuildingInvocationHandler(source, path = []) {
    this.source = source
    this.path = path
  }

  @Override
  Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    path << Configurations.fromBeanSpec(method.name)
    if (returnsAValue(method)) return source.lookup(path)
    else null
  }

  boolean returnsAValue(Method method) {
    true
  }
}
