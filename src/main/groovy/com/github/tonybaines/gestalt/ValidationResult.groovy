package com.github.tonybaines.gestalt

import groovy.transform.Immutable

public class ValidationResult implements Iterable<ValidationResult> {
  def items = []

  def leftShift(ValidationResult.Item item) {
    items << item
  }

  @Override
  Iterator<ValidationResult> iterator() {
    items.iterator()
  }

  boolean hasFailures() {
    !(items.isEmpty())
  }

  @Immutable
  public static final class Item {
    final String property, message
  }
}