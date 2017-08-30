package com.github.tonybaines.gestalt.transformers

import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

class PropertyTypeTransformerSpecFixture {
    static class Foo {
        Foo(String x){}
    }
    static class Bar {
        Bar() {}
    }
    static class MyTransformations {
        static Foo makeFoo(String x) {
            new Foo(x)
        }

        static String fromFoo(Foo x) {
            new String('I am Foo')
        }

        static Path makePath(String pathString) {
            Paths.get(pathString)
        }
    }

    static class Duplicates {
        static Instant makeInstant1(String timestamp) {
            throw new UnsupportedOperationException("Should not be used")
        }

        static Instant makeInstant2(String timestamp) {
            throw new UnsupportedOperationException("Should not be used")
        }

        static String fromInstant1(Instant timestamp) {
            throw new UnsupportedOperationException("Should not be used")
        }

        static String fromInstant2(Instant timestamp) {
            throw new UnsupportedOperationException("Should not be used")
        }
    }

    static class Broken {
        static Integer makeInt(String s) {
            throw new NumberFormatException("TEST")
        }
        static String fromInt(Integer i) {
            throw new NumberFormatException("TEST")
        }
    }

    static class MultipleParams {
        static Integer makeInt(String s, int radix) {
            throw new UnsupportedOperationException("Should not be used")
        }

        static String fromInt(Integer x, String base) {
            throw new UnsupportedOperationException("Should not be used")
        }
    }
    static class InstanceMethod {
        Integer makeInt(String s) {
            throw new UnsupportedOperationException("Should not be used")
        }

        String fromInt(Integer x) {
            throw new UnsupportedOperationException("Should not be used")
        }
    }
}
