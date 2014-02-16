package tonybaines.configuration

import java.lang.reflect.Method

class ConfigurationException extends RuntimeException {
  ConfigurationException(Method method, Throwable cause) {
    super("Failed to handle ${method.name}", cause)
  }
}
