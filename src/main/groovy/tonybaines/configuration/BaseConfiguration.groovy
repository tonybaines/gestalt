package tonybaines.configuration

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/*
 TODO: try building up paths e.g. ['config', 'strings'] and evaluating against the underlying store
 TODO: Filtering is the responsibility of client code, not config. 'id' is NOT a special case
  */

abstract class BaseConfiguration<T> implements Configurations.Configuration<T> {
  protected Class configInterface

  public BaseConfiguration(Class configInterface) {
    this.configInterface = configInterface
  }

  abstract T load()

  static isAList(type) { type instanceof ParameterizedType && type.rawType.isAssignableFrom(List) }

  static abstract class ConfigurationInvocationHandler implements InvocationHandler {

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      try {
        def node = lookUp(Configurations.fromBeanSpec(method.name))

        if (isAList(method.genericReturnType)) {
          return handleList(node, method)
        } else if (method.returnType.enum) {
          return method.returnType.valueOf(valueOf(node))
        } else {
          return decoded(node, method.returnType)
        }
      } catch (Throwable e) {
        throw new ConfigurationException(method, e)
      }
    }

    protected def decoded(node, returnType) {
      switch (returnType) {
        case String: return valueOf(node)
        case Integer: return valueOf(node).toInteger()
        case Double: return valueOf(node).toDouble()
        case Boolean: return valueOf(node).toBoolean()

        default: return around(returnType, node)
      }
    }

    protected abstract String valueOf(x);

    protected abstract lookUp(String methodName);

    protected abstract handleList(node, method);

    public abstract def around(Class configInterface, configSource);
  }

}
