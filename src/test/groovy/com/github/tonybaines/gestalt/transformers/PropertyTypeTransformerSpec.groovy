package com.github.tonybaines.gestalt.transformers

import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Path
import java.time.Instant

import static com.github.tonybaines.gestalt.transformers.PropertyTypeTransformerSpecFixture.*

class PropertyTypeTransformerSpec extends Specification {
    @Shared
    private PropertyTypeTransformer tx = PropertyTypeTransformer.from(MyTransformations.class)


    def "Can find and use a static transformation function by destination type"() {
        when:
        Foo foo = this.tx.fromString("foo", Foo.class)
        Path path = this.tx.fromString("/tmp", Path.class)

        then:
        foo != null
        path != null
    }

    def "A type without a transformation function yields a 'null' value"() {
        expect:
        this.tx.fromString("bar", Bar.class) == null
    }

    def "Ignores any transformation with more than one candidate function"() {
        expect:
        PropertyTypeTransformer
                .from(Duplicates.class)
                .fromString("1970-01-01T00:00:00", Instant.class) == null
    }

    def "Rethrows any exception raised during transformation"() {
        when:
        PropertyTypeTransformer
                .from(Broken.class)
                .fromString("foobar", Integer.class)

        then:
        thrown(NumberFormatException)
    }

    def "Ignores instance methods"() {
        expect:
        PropertyTypeTransformer
                .from(InstanceMethod.class)
                .fromString("foobar", Integer.class) == null

    }

    def "Ignores static methods which don't accept a single String argument"() {
        when:
        expect:
        PropertyTypeTransformer
                .from(MultipleParams.class)
                .fromString("foobar", Integer.class) == null

    }
}
