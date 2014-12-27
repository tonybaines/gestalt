package com.github.tonybaines.gestalt.transformers

import spock.lang.Specification
import spock.lang.Unroll

class HyphenatedPropertyNameTransformerSpec extends Specification {
  def transformer = new HyphenatedPropertyNameTransformer()

  @Unroll
  def "Converts from property names and back again [#propName <-> #transformed]"() {
    expect:
    transformer.fromPropertyName(propName) == transformed
    transformer.toPropertyName(transformed) == propName

    where:
    propName           | transformed
    'single'           | 'single'
    'twoWord'          | 'two-word'
    'moreThanTwoWords' | 'more-than-two-words'
  }

}
