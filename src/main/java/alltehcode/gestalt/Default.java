package alltehcode.gestalt;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface Default {
  @Target({METHOD})
  @Retention(RUNTIME)
  public @interface String {
    java.lang.String value();
  }

  @Target({METHOD})
  @Retention(RUNTIME)
  public @interface Integer {
    int value();
  }

  @Target({METHOD})
  @Retention(RUNTIME)
  public @interface Boolean {
    boolean value();
  }

  @Target({METHOD})
  @Retention(RUNTIME)
  public @interface Double {
    double value();
  }

  @Target({METHOD})
  @Retention(RUNTIME)
  public @interface Enum {
    java.lang.String value();
  }
}
