package tonybaines.configuration

import groovy.util.logging.Slf4j

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

@Slf4j
abstract class BaseConfiguration<T> implements Configurations.Configuration<T> {
  protected Class configInterface
  protected final boolean validateOnLoad

  public BaseConfiguration(Class configInterface, boolean validateOnLoad) {
    this.configInterface = configInterface
    this.validateOnLoad = validateOnLoad
  }

  abstract T load()

  static abstract class ConfigurationInvocationHandler implements InvocationHandler {

    @Override
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.name.equals("hashCode")) return 1
      if (method.name.equals("equals")) return false
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
        log.warn "Failed to find a definition for ${method.name} in ${this.class.simpleName.replace('ConfigProxy', '')}"
        throw new ConfigurationException(method, e)
      }
    }

    static isAList(type) { type instanceof ParameterizedType && type.rawType.isAssignableFrom(List) }

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

    /**
     * Warning - this method is called recursively with a sub-tree of
     * the current configuration.  This usually means a new instance
     * of the specific InvocationHandler (not reused)
     */
    public abstract def around(Class configInterface, configSource);
  }

}
