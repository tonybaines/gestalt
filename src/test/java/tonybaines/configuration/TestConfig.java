package tonybaines.configuration;


import javax.validation.constraints.NotNull;
import java.util.List;

public interface TestConfig {
  @NotNull()
  Integer getIntValue();

  String getStringValue();

  Double getDoubleValue();

  Boolean getBooleanValue();

  Handed getHandedness();

  SubConfigLevel1 getSubConfig();

  List<String> getStrings();

  List<Thing> getThings();

  Integer getNonExistent();

  @Default.String("default-value")
  String getNonExistentStringWithDefault();

  @Default.Integer(42)
  Integer getNonExistentIntegerWithDefault();

  @Default.Boolean(false)
  Boolean getNonExistentBooleanWithDefault();

  @Default.Double(42.5)
  Double getNonExistentDoubleWithDefault();

  /**
   * Enums need to have defaults defined
   * as String constants such that they
   * evaluate successfully from
   * <pre>MyEnum.valueOf(string)</pre>
   */
  @Default.Enum("right")
  Handed getNonExistentEnumWithDefault();

  String getPropertyDefinedOnlyInGroovyConfig();

  String getPropertyDefinedAllConfigSources();

  public static interface SubConfigLevel1 {
    Integer getIntValue();
  }

  public static interface Thing {
    String getId();

    String getStringValue();
  }
}
