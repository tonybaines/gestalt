package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method

class PathBuildingInvocationHandler<T> implements InvocationHandler {
  private def source

  PathBuildingInvocationHandler(source) {
    this.source = source
  }

  def around(configInterface, path = []) {
    java.lang.reflect.Proxy.newProxyInstance(this.class.classLoader, (Class[]) [configInterface], new PathBuildingInvocationHandler(source))
  }

  @Override
  Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return buildPath(method)
  }

  protected buildPath(Method method, path = []) {
    path << Configurations.fromBeanSpec(method.name)
    if (returnsAValue(method)) return source.lookup(path)
  }

  boolean returnsAValue(Method method) {
    switch (method.returnType) {
      case String: return true
      case Integer: return true
      case Double: return true
      case Boolean: return true

      default: return false
    }
  }
}
