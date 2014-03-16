package com.github.tonybaines.gestalt

import javax.validation.Validation
import javax.validation.executable.ExecutableValidator

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
        failures << new ValidationResult.Item("${pathSoFar}.${propertyName}", (e?.cause?.message ?: "Is undefined.  See the logs for more details"))
      }
    }
  }
}
