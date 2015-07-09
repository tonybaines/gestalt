package com.github.tonybaines.gestalt.sources.features

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.Configurations
import groovy.util.logging.Slf4j

import javax.validation.Validation
import javax.validation.Validator
import javax.validation.metadata.BeanDescriptor
import java.beans.PropertyDescriptor
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
    def propertyName = Configurations.Utils.fromBeanSpec(method.name)

    // Only attempt validation if the property has a JSR-303 constraint applied
    def constraintsForClass = validator.getConstraintsForClass(method.declaringClass)
    def propertyIsConstrained = constraintsForClass.constrainedProperties.any { it.propertyName == propertyName }

    if (propertyIsConstrained) {
      def validationResults = validator.validateValue(method.declaringClass, propertyName, value)
      if (!validationResults.empty) {1
        validationResults.each {
          log.warn "Validation failed for ${it.propertyPath.first()}: ${it.message} (was $value)"
        }
        return null
      }
    }
    return value
  }
}
