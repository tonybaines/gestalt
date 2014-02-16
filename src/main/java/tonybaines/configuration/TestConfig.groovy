package tonybaines.configuration

public interface TestConfig {
  Integer intValue();

  String stringValue();

  Double doubleValue();

  Boolean booleanValue();

  Handed handedness();

  SubConfigLevel1 subConfig();

  // Only List currently supported for collective types
  List<String> strings();

  public interface SubConfigLevel1 {
    Integer intValue();
  }
}