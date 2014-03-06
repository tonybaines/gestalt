package com.github.tonybaines.gestalt

import java.lang.reflect.Method

class ConfigurationException extends RuntimeException {
  ConfigurationException(String message, Exception cause) {
    super(message, cause)
  }

  ConfigurationException(Method method, Throwable cause) {
    super("Failed to handle ${method.name}", cause)
  }

  ConfigurationException(String methodName, String reason) {
    super("Failed to handle $methodName: ${reason}")
  }

  ConfigurationException(String... messages) {
    super(messages.join('\n'))
  }
}
