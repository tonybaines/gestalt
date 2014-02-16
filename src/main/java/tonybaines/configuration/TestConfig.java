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

  void getNonExistent();

  public static interface SubConfigLevel1 {
    Integer getIntValue();
  }

  public static interface Thing {
    String getId();
    String getStringValue();
  }
}
