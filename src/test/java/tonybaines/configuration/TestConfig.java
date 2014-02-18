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

  String getPropertyDefinedOnlyInGroovyConfig();

  public static interface SubConfigLevel1 {
    Integer getIntValue();
  }

  public static interface Thing {
    String getId();
    String getStringValue();
  }
}
