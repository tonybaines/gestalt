package com.github.tonybaines.gestalt;

import com.github.tonybaines.gestalt.validation.ValidationResult;
import com.google.common.collect.Lists;

public interface CustomValidationConfig {

  default ValidationResult validateMany() {
    ValidationResult result = new ValidationResult();
    result.add(new ValidationResult.Item("test-many", "all OK", Lists.newArrayList()));
    return result;
  }
  default ValidationResult.Item validateOne() {
    return new ValidationResult.Item("test-one", "all OK", Lists.newArrayList());
  }
}
