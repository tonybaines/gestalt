package tonybaines.configuration

public interface TestConfig {
  Integer intValue();

  String stringValue();

  Double doubleValue();

  SubConfigLevel1 subConfig();

  public interface SubConfigLevel1 {
    Integer intValue();
  }
}