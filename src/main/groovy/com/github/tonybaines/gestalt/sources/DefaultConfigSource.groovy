package com.github.tonybaines.gestalt.sources

import com.github.tonybaines.gestalt.ConfigSource
import com.github.tonybaines.gestalt.ConfigurationException
import com.github.tonybaines.gestalt.Configurations
import com.github.tonybaines.gestalt.Default
import groovy.util.logging.Slf4j

import java.lang.annotation.Annotation
import java.lang.reflect.Method

import static com.github.tonybaines.gestalt.Configurations.Utils.hasAFromStringMethod

@Slf4j
class DefaultConfigSource implements ConfigSource {
  @Override
  def lookup(List<String> path, Method method) {
    Annotation defaultAnnotation = method.declaredAnnotations.find {
      it.annotationType().name.contains(Default.class.name)
    }
    if (defaultAnnotation != null) {
      log.info "Falling back to default definition for property '${path.join('.')}'"
      Class<? extends Annotation> defaultsType = defaultAnnotation.annotationType()

      if (Configurations.Utils.isAList(method.genericReturnType) &&
                Default.EmptyList.class.name.equals(defaultsType.name)) {
        return new ArrayList()
      }

      def annotationValue = method.getAnnotation(defaultsType).value()

      if (Default.Enum.class.name.equals(defaultsType.name)) {
          try {
              return method.returnType.valueOf(annotationValue)
          }
          catch (IllegalArgumentException e) {
              throw new ConfigurationException(method, e)
          }
      } else if (hasAFromStringMethod(method.returnType)) {
          return (annotationValue != null) ? method.returnType.fromString(annotationValue) : null
      } else {
        return annotationValue
      }
    } else log.info "Failed to find a default definition for property '${path.join('.')}'"

    return null
  }
}
