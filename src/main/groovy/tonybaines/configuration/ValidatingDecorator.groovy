package tonybaines.configuration

import javax.validation.Validation
import javax.validation.Validator
import java.lang.reflect.GenericDeclaration
import java.lang.reflect.Method

class ValidatingDecorator<T> implements ConfigSource {
  private final GenericDeclaration configInterface
  private final ConfigSource configSource

  ValidatingDecorator(Class configInterface, ConfigSource configSource) {
    this.configInterface = configInterface
    this.configSource = configSource
  }

  @Override
  def lookup(List<String> path, Method method) {
    def value = configSource.lookup(path, method)
    if (value != null && Configurations.isAValueType(value.class)) {
      validate(method)
    }
    value
  }

  def validate(Method method) {
    def property = Configurations.fromBeanSpec(method.name)
    Validator validator = Validation.buildDefaultValidatorFactory().validator
    def validationResults = validator.validateProperty(new DynoClass<T>(configSource).getMapAsInterface(method.declaringClass), property)
    if (!validationResults.empty) {
      throw new ConfigurationException(validationResults.collect { "${it.propertyPath} ${it.interpolatedMessage}" })
    }
  }
}
