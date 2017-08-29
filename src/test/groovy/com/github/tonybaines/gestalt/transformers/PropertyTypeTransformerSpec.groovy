package com.github.tonybaines.gestalt.transformers

import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class PropertyTypeTransformerSpec extends Specification {
    static class Foo {
        String x

        Foo(String x) {
            this.x = x
        }
    }
    static class MyTransformations {
        static Foo makeFoo(String x) {
            new Foo(x)
        }

        static Path makePath(String pathString) {
            Paths.get(pathString)
        }
    }

    def "can find a transformation function by destination type"() {
        given:
        PropertyTypeTransformer tx = PropertyTypeTransformer.from(MyTransformations.class)

        when:
        Foo foo = tx.transform("foo", Foo.class)
        Path path = tx.transform("/tmp", Path.class)

        then:
        foo != null
        path != null
    }
}
