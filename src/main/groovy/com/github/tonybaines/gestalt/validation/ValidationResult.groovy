package com.github.tonybaines.gestalt.validation

import com.google.common.collect.Lists
import groovy.transform.Immutable

public class ValidationResult implements Iterable<ValidationResult> {
  def items = []

  def add(Item item) {
    leftShift(item)
  }

  def leftShift(result) {
    if (result != null) {
      if (result instanceof Item) {
        items << result
      }
      else if(result instanceof ValidationResult)  {
        items.addAll(result.items.grep{it != null})
      }
    }
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

  public static Item item(String property, String message) {
    return new Item(property, message, Lists.newArrayList())
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
