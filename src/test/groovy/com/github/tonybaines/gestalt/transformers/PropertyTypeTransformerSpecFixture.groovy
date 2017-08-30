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

        static Path makePath(String pathString) {
            Paths.get(pathString)
        }
    }

    static class Duplicates {
        static Instant makeInstant1(String timestamp){
            Instant.parse(timestamp)
        }

        static Instant makeInstant2(String timestamp){
            Instant.parse(timestamp)
        }
    }

    static class Broken {
        static Integer makeInt(String s) {
            throw new NumberFormatException("TEST")
        }
    }

    static class MultipleParams {
        static Integer makeInt(String s, int radix) {
            1
        }
    }
    static class InstanceMethod {
        Integer makeIntInstance(String s) {
            throw new UnsupportedOperationException("Should not be used")
        }
    }
}
