package com.github.tonybaines.gestalt;

import com.github.tonybaines.gestalt.validation.ValidationResult;


public interface CustomValidationConfig {
  @Optional
  String getBar();

  @Optional
  String getFoo();

  @Optional
  String getBaz();

  default ValidationResult validateNotFooAndBar(CustomValidationConfig instance) {
    ValidationResult result = new ValidationResult();

    if (instance.getFoo() != null && instance.getBar() != null) {
      result.add(ValidationResult.item("foo", "Only Foo *or* Bar should be defined"));
      result.add(ValidationResult.item("bar", "Only Foo *or* Bar should be defined"));
    }

    return result;
  }


  default ValidationResult.Item validateFoo(CustomValidationConfig instance) {
    if ("baz".equals(instance.getFoo()) && "baz".equals(instance.getBaz())) {
      return ValidationResult.item("foo", "foo cannot be 'baz' if baz is also baz");
    }
    return null;
  }
}
