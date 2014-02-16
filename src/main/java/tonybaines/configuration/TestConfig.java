package tonybaines.configuration;

import java.util.List;

public interface TestConfig {
  public abstract Integer intValue();

  public abstract String stringValue();

  public abstract Double doubleValue();

  public abstract Boolean booleanValue();

  public abstract Handed handedness();

  public abstract SubConfigLevel1 subConfig();

  public abstract List<String> strings();

  public static interface SubConfigLevel1 {
    public abstract Integer intValue();
  }
}
