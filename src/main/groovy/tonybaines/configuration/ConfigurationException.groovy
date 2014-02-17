package tonybaines.configuration

import java.lang.reflect.Method

class ConfigurationException extends RuntimeException {
  ConfigurationException(Method method, Throwable cause) {
    super("Failed to handle ${method.name}", cause)
  }

  ConfigurationException(String methodName, String reason) {
    super("Failed to handle $methodName: ${reason}")
  }
}
