package com.github.tonybaines.gestalt.transformers;

public interface PropertyNameTransformer {
  public abstract String fromPropertyName(String propertyName);
  public abstract String toPropertyName(String value);
}
