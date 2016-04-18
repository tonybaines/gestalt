package com.github.tonybaines.gestalt.validation

import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.Configurations
import groovy.util.logging.Slf4j

import static com.github.tonybaines.gestalt.Configurations.Utils.declaredMethodsOf
import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod
import static com.github.tonybaines.gestalt.Configurations.Utils.isNotAProperty
import static com.github.tonybaines.gestalt.Configurations.Utils.optional
import static com.github.tonybaines.gestalt.Configurations.Utils.returnsAValue
import static com.github.tonybaines.gestalt.Configurations.Utils.isDefaultReturningValidationResults

@Slf4j
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
    recursiveValidation(instance, configInterface)
    failures
  }

  private def recursiveValidation(object, configInterface, pathSoFar = "") {
    declaredMethodsOf(configInterface).each { method ->
      String propertyName = Configurations.Utils.fromBeanSpec(method.name)
      try {

        if (isDefaultReturningValidationResults(method)) {
          try {
            def validationResults = object.invokeMethod(method.name, object)
            failures << validationResults
          } catch (ConfigurationException ignored) {
            // Ignore config exceptions thrown during custom validation
            log.warn("While running custom validation ${configInterface.name}#${method.name}: ${ignored.getMessage()}")
          }
          return
        }

        if (isNotAProperty(object, propertyName)) return

        def value = object."${propertyName}"

        // Simple values, enums and custom types
        if (returnsAValue(method) || method.returnType.enum || hasAFromStringMethod(method.returnType) ) return

        // Lists of values
        if (Configurations.Utils.isAList(method.genericReturnType)) {
          Class listGenericType = method.genericReturnType.actualTypeArguments[0]

          if (!Configurations.Utils.isAValueType(listGenericType)) {
            // A list of sub-types
            value.each { item -> recursiveValidation(item, listGenericType, fullPath(pathSoFar, propertyName)) }
          }
        }
        // a single sub-type
        else {
          if (value != null) recursiveValidation(value, method.returnType, fullPath(pathSoFar, propertyName))
        }
      } catch (ConfigurationException e) {
        if (!optional(method)) {
          failures << new ValidationResult.Item(fullPath(pathSoFar, propertyName), (e?.cause?.message ?: "Is undefined."), Configurations.Utils.annotationInfo(method))
        }
      }
    }
  }


  private static fullPath(pathSoFar, propertyName) {
    (pathSoFar.isEmpty() ? '' : "${pathSoFar}.") + "${propertyName}"
  }

}
