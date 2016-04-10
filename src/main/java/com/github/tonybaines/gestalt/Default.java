package com.github.tonybaines.gestalt;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

public interface Default {
    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface String {
        java.lang.String value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Integer {
        int value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Boolean {
        boolean value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Double {
        double value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Long {
        long value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface Enum {
        java.lang.String value();
    }

    @Target({ METHOD })
    @Retention(RUNTIME)
    @interface EmptyList {
    }
}
