package com.github.tonybaines.gestalt

import javax.validation.Validation
import javax.validation.constraints.*
import javax.validation.executable.ExecutableValidator
import java.lang.annotation.Annotation
import java.lang.reflect.Method

class ReflectionValidator {
  private final Object instance
  private final Class configInterface
  private failures = new ValidationResult()

  public ReflectionValidator(Object instance, Class configInterface) {
    this.configInterface = configInterface
    this.instance = instance
  }

  ValidationResult validate() {
    failures = new ValidationResult()
    recursiveValidation(instance, configInterface, configInterface.simpleName)
    failures
  }

  private def recursiveValidation(object, configInterface, pathSoFar = "") {
    configInterface.declaredMethods.each { method ->
      String propertyName = Configurations.Utils.fromBeanSpec(method.name)
      try {
        def value = object."${propertyName}"

        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().validator.forExecutables()
        def validationResults = validator.validateReturnValue(configInterface, method, value)

        // Simple values
        if (Configurations.Utils.returnsAValue(method) || method.returnType.enum) return

        // Lists of values
        if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]

          if (!Configurations.Utils.isAValueType(listGenericType)) {
            // A list of sub-types
            value.each { item -> recursiveValidation(item, listGenericType, "${pathSoFar}.${propertyName}") }
          }
        }
        // a single sub-type
        else {
          if (value != null) recursiveValidation(value, method.returnType, "${pathSoFar}.${propertyName}")
        }
      } catch (ConfigurationException e) {
        failures << new ValidationResult.Item("${pathSoFar}.${propertyName}", (e?.cause?.message ?: "Is undefined."), annotationInfo(method))
      }
    }
  }

  private def annotationInfo(Method method) {
    def info = []
    method.declaredAnnotations.each { Annotation a ->
      Class type = a.annotationType()

      if (type.name.contains(Default.class.name)) {
        def defaultValue = a.h.memberValues['value']
        info << "default: ${defaultValue}"
      }

      if (type.canonicalName.startsWith('javax.validation.constraints')) {
        switch (type) {
          case Size: info << "[Size: min=${a.min()}, max=${a.max()}]"; break
          case AssertTrue: info << "[Always true]"; break
          case AssertFalse: info << "[Always false]"; break
          case DecimalMin: info << "[Decimal min=${a.value()}]"; break
          case DecimalMax: info << "[Decimal max=${a.value()}]"; break
          case Digits: info << "[Digits integer-digits=${a.integer()}, fraction-digits=${a.fraction()}]"; break
          case Min: info << "[Min ${a.value()}]"; break
          case Max: info << "[Max ${a.value()}]"; break
          case NotNull: info << "[Not Null]"; break
          case Null: info << "[Always Null]"; break
          case Pattern: info << "[Pattern ${a.regexp()}]"; break
        }
      }
    }
    info
  }
}
