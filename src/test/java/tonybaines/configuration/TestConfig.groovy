package tonybaines.configuration

public interface TestConfig {
  Integer intValue();

  String stringValue();

  Double doubleValue();

  Boolean booleanValue();

  SubConfigLevel1 subConfig();

  List<String> strings();

  public interface SubConfigLevel1 {
    Integer intValue();
  }
}