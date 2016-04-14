package com.github.tonybaines.gestalt.validation

import groovy.transform.Immutable

public class ValidationResult implements Iterable<ValidationResult> {
  def items = []

  def add(Item item) {
    leftShift(item)
  }
  
  def leftShift(Item item) {
    items << item
  }
  
  def leftShift(ValidationResult result) {
    items.addAll(result.items)
  }

  @Override
  Iterator<ValidationResult> iterator() {
    items.iterator()
  }

  boolean hasFailures() {
    !(items.isEmpty())
  }

  @Override
  String toString() {
    items.join('\n')
  }

  @Immutable
  public static final class Item {
    final String property, message
    final List<String> propertyMetadata

    @Override
    String toString() {
      return "$property (${propertyMetadata.join(', ')}) - $message"
    }
  }
}
