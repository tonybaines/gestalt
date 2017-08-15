package com.github.tonybaines.gestalt

import com.github.tonybaines.gestalt.sources.features.CachingDecorator
import spock.lang.Specification

import java.lang.reflect.Method

class CachingSpec extends Specification {
  def "A caching decorator will keep and reuse previous requests"() {
    given:
    def method = Object.getMethod("toString")
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    2.times { cachingSource.lookup(['one', 'two'], method) }

    then:
    1 * realSource.lookup(['one', 'two'], method) >> 'foo'
  }

  def "A caching decorator won't cache null values"() {
    given:
    def method = Object.getMethod("toString")
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    2.times { cachingSource.lookup(['one', 'two'], method) }

    then:
    2 * realSource.lookup(['one', 'two'], method) // returns 'null' by default
  }

  def "A caching decorator will rethrow any exception"() {
    given:
    def method = Object.getMethod("toString")
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    cachingSource.lookup(['one', 'two'], method)

    then:
    1 * realSource.lookup(['one', 'two'], method) >> { throw new IllegalStateException("TEST") }
    thrown(IllegalStateException)
  }
}