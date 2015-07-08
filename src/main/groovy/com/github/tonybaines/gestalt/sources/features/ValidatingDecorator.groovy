package com.github.tonybaines.gestalt.sources.features

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.DynoClass
import groovy.util.logging.Slf4j

import javax.validation.Validation
import javax.validation.Validator
import java.lang.reflect.Method

@Slf4j
class ValidatingDecorator<T> implements ConfigSource {
  private final ConfigSource configSource

  ValidatingDecorator(ConfigSource configSource) {
    this.configSource = configSource
  }

  @Override
  def lookup(List<String> path, Method method) {
    def value = configSource.lookup(path, method)
    if (value != null && Configurations.Utils.isAValueType(value.class)) {
      return validate(method, value)
    }
    value
  }

  def validate(Method method, value) {
    Validator validator = Validation.buildDefaultValidatorFactory().getValidator()
    def validationResults = validator.validateValue(method.declaringClass, Configurations.Utils.fromBeanSpec(method.name), value)
    if (!validationResults.empty) {
      validationResults.each {
        log.warn "Validation failed for ${it.propertyPath.first()}: ${it.message} (was $value)"
      }
      return null
    }
    return value
  }
}
