package com.github.tonybaines.gestalt.transformers

import static com.google.common.base.CaseFormat.LOWER_CAMEL
import static com.google.common.base.CaseFormat.LOWER_HYPHEN

class HyphenatedPropertyNameTransformer implements PropertyNameTransformer {
  String fromPropertyName(String propertyName) { LOWER_CAMEL.to(LOWER_HYPHEN, propertyName) }
}
