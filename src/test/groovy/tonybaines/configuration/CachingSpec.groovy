package tonybaines.configuration

import spock.lang.Specification
import tonybaines.configuration.sources.features.CachingDecorator

import java.lang.reflect.Method


class CachingSpec extends Specification {
  def "A caching decorator will keep and reuse previous requests"() {
    given:
    Method method = GroovyStub()
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    2.times { cachingSource.lookup(['one', 'two'], method) }

    then:
    1 * realSource.lookup(['one', 'two'], method) >> 'foo'
  }

  def "A caching decorator won't cache null values"() {
    given:
    Method method = GroovyStub()
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    2.times { cachingSource.lookup(['one', 'two'], method) }

    then:
    2 * realSource.lookup(['one', 'two'], method) // returns 'null' by default
  }

  def "A caching decorator will rethrow any exception"() {
    given:
    Method method = GroovyStub()
    ConfigSource realSource = Mock()
    ConfigSource cachingSource = new CachingDecorator(realSource)

    when:
    cachingSource.lookup(['one', 'two'], method)

    then:
    1 * realSource.lookup(['one', 'two'], method) >> { throw new IllegalStateException("TEST") }
    thrown(IllegalStateException)
  }
}